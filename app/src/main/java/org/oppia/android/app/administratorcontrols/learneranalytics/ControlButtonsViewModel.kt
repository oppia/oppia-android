package org.oppia.android.app.administratorcontrols.learneranalytics

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.protobuf.MessageLite
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.zip.GZIPOutputStream
import javax.inject.Inject

/**
 * [ProfileListItemViewModel] that represents the portion of the learner analytics admin page which
 * provides some control buttons for ID/logs sharing and syncing.
 */
@SuppressLint("StaticFieldLeak") // False positive (Android doesn't manage this model's lifecycle).
class ControlButtonsViewModel private constructor(
  private val oppiaLogger: OppiaLogger,
  private val activity: AppCompatActivity,
  private val analyticsController: AnalyticsController,
  private val syncStatusManager: SyncStatusManager,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val viewModels: List<ProfileListItemViewModel>
) : ProfileListItemViewModel(ProfileListViewModel.ProfileListItemViewType.SHARE_IDS) {
  private var monitoredUploadProgress: LiveData<ForceSyncProgress> =
    MutableLiveData(ForceSyncProgress())

  /** The current [ForceSyncProgress], as affected by [onUploadLogsNowButtonClicked]. */
  val forceUploadProgress: LiveData<ForceSyncProgress> get() = monitoredUploadProgress

  /** The current [SyncStatus] of event logs. */
  val syncStatus: LiveData<SyncStatus> by lazy {
    Transformations.map(syncStatusManager.getSyncStatus().toLiveData(), this::processSyncStatus)
  }

  /**
   * Indicates the user wants to share learner & device IDs, along with event log metrics, with
   * another app.
   */
  fun onShareButtonClicked() {
    // Reference: https://developer.android.com/guide/components/intents-common#Email from
    // https://stackoverflow.com/a/15022153/3689782.
    val logs = retrieveEventLogs(viewModels.filterIsInstance<ProfileLearnerIdItemViewModel>())
    val sharedText = viewModels.mapNotNull { viewModel ->
      when (viewModel) {
        is DeviceIdItemViewModel -> listOf("Oppia app installation ID: ${viewModel.deviceId.value}")
        is ProfileLearnerIdItemViewModel -> {
          val profile = viewModel.profile
          val stats = viewModel.profileSpecificEventsUploadStats.value
          val learnerStats = stats?.learnerStats
          val uncategorizedStats = stats?.uncategorizedStats
          listOfNotNull(
            "- Profile name: ${profile.name}, learner ID: ${profile.learnerId}",
            learnerStats?.awaitingUploadEventCountText?.let { "  - Uploading learner events: $it" },
            learnerStats?.uploadedEventCountText?.let { "  - Uploaded learner events: $it" },
            uncategorizedStats?.awaitingUploadEventCountText?.let {
              "  - Uploading uncategorized events: $it"
            },
            uncategorizedStats?.uploadedEventCountText?.let {
              "  - Uploaded uncategorized events: $it"
            }
          )
        }
        is SyncStatusItemViewModel -> {
          val halfLineCount = BASE64_LINE_WRAP_LIMIT / 2
          val logsStr = logs?.toCompressedBase64()
          listOf(
            "Current sync status: ${viewModel.syncStatus.value}.",
            "Event log encoding integrity checks:",
            "- First $halfLineCount chars of encoded string: ${logsStr?.take(halfLineCount)}",
            "- Last $halfLineCount chars of encoded string: ${logsStr?.takeLast(halfLineCount)}",
            "- SHA-1 hash (unwrapped event string): ${logsStr?.computeSha1Hash(machineLocale)}",
            "- Total event string length (unwrapped): ${logsStr?.length}",
            "Encoded event logs:"
          ) + (logsStr?.chunked(BASE64_LINE_WRAP_LIMIT) ?: listOf("Missing"))
        }
        else -> null
      }
    }.flatten().joinToString(separator = "\n")
    try {
      activity.startActivity(
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          putExtra(Intent.EXTRA_TEXT, sharedText)
        }
      )
    } catch (e: ActivityNotFoundException) {
      oppiaLogger.e("ControlButtonsViewModel", "No activity found to receive shared IDs.", e)
    }
  }

  /**
   * Indicates that the user wishes to upload any pending event logs immediately, rather than
   * waiting for the app to schedule an event upload.
   *
   * The status of the upload result can be observed via [forceUploadProgress].
   *
   * [canStartUploadingLogs] should be used to determine whether now is a valid time to start
   * uploading logs.
   */
  fun onUploadLogsNowButtonClicked() {
    monitoredUploadProgress =
      Transformations.map(
        analyticsController.uploadEventLogs().toLiveData(), this::processUploadedEventLogs
      )
    notifyChange() // Recompute bindings since the live data instance has changed.
  }

  /**
   * Returns whether current conditions, per [isCurrentlyUploading] and [syncStatus], indicate that
   * there are events that can be manually force-uploaded by the user.
   *
   * Note that both [isCurrentlyUploading] and [syncStatus] permit null values because of an issue
   * with databinding wherein multiple [LiveData]s bound at the same time may result in one or both
   * of the values correspond to the *current* [LiveData] value which may be null if the [LiveData]
   * hasn't been updated yet (even if its type indicates non-nullness). Permitting null allows the
   * implementation to work around this case by assuming that either value missing means no
   * determination can be made whether logs can start being uploaded.
   */
  fun canStartUploadingLogs(isCurrentlyUploading: Boolean?, syncStatus: SyncStatus?): Boolean =
    isCurrentlyUploading == false && syncStatus == SyncStatus.WAITING_TO_START_UPLOADING

  private fun retrieveEventLogs(viewModels: List<ProfileLearnerIdItemViewModel>): OppiaEventLogs? =
    viewModels.firstOrNull()?.oppiaEventLogs?.value?.let { processEventLogs(it) }

  private fun processUploadedEventLogs(result: AsyncResult<Pair<Int, Int>>): ForceSyncProgress {
    return when (result) {
      is AsyncResult.Pending -> ForceSyncProgress(eventsUploaded = 0, totalEventsToUpload = 0)
      is AsyncResult.Success -> {
        val (currentUploadedEventCount, totalEventCount) = result.value
        ForceSyncProgress(currentUploadedEventCount, totalEventCount)
      }
      is AsyncResult.Failure -> ForceSyncProgress().also {
        oppiaLogger.e(
          "ControlButtonsViewModel", "Encountered failure while uploading events.", result.error
        )
      }
    }
  }

  private fun processSyncStatus(result: AsyncResult<SyncStatus>): SyncStatus {
    return when (result) {
      is AsyncResult.Pending -> SyncStatus.INITIAL_UNKNOWN
      is AsyncResult.Success -> result.value
      is AsyncResult.Failure -> SyncStatus.INITIAL_UNKNOWN.also {
        oppiaLogger.e(
          "ControlButtonsViewModel",
          "Encountered failure while retrieving sync status.",
          result.error
        )
      }
    }
  }

  private fun processEventLogs(result: AsyncResult<OppiaEventLogs>): OppiaEventLogs? {
    return when (result) {
      is AsyncResult.Pending -> null
      is AsyncResult.Success -> result.value
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ControlButtonsViewModel", "Encountered failure while on-disk event logs.", result.error
        )
        null
      }
    }
  }

  /**
   * Progress representation structure that provides the UI with an indication of how much of an
   * upload event operation has completed, and how much remains.
   *
   * Note that a [totalEventsToUpload] value of 0 indicates there are 0 left in the **current**
   * operation, but there may have been events to upload in the past.
   *
   * @property eventsUploaded the number of events that have uploaded so far
   * @property totalEventsToUpload the total number of events to upload, or none, or
   *     [DEFAULT_UNKNOWN_EVENTS_TO_UPLOAD_COUNT] if it's not yet known how many events can be
   *     uploaded
   */
  data class ForceSyncProgress(
    val eventsUploaded: Int = 0,
    val totalEventsToUpload: Int = DEFAULT_UNKNOWN_EVENTS_TO_UPLOAD_COUNT
  ) {
    /** Returns whether there are events that can be uploaded. */
    fun hasEventsToUpload(): Boolean = totalEventsToUpload != DEFAULT_UNKNOWN_EVENTS_TO_UPLOAD_COUNT

    /** Returns whether there are events currently being uploaded. */
    fun isCurrentlyUploading(): Boolean = eventsUploaded < totalEventsToUpload
  }

  /** Factory for creating new [ControlButtonsViewModel]s. */
  class Factory @Inject constructor(
    private val oppiaLogger: OppiaLogger,
    private val activity: AppCompatActivity,
    private val analyticsController: AnalyticsController,
    private val syncStatusManager: SyncStatusManager,
    private val machineLocale: OppiaLocale.MachineLocale
  ) {
    /** Returns a new [ControlButtonsViewModel]. */
    fun create(viewModels: List<ProfileListItemViewModel>): ControlButtonsViewModel {
      return ControlButtonsViewModel(
        oppiaLogger, activity, analyticsController, syncStatusManager, machineLocale, viewModels
      )
    }
  }

  private companion object {
    private const val BASE64_LINE_WRAP_LIMIT = 80
    private const val DEFAULT_UNKNOWN_EVENTS_TO_UPLOAD_COUNT = Integer.MIN_VALUE

    // Copied from ProtoStringEncoder (which isn't available in production code).
    private fun <M : MessageLite> M.toCompressedBase64(): String {
      val compressedMessage = ByteArrayOutputStream().also { byteOutputStream ->
        GZIPOutputStream(byteOutputStream).use(::writeTo)
      }.toByteArray()
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getEncoder().encodeToString(compressedMessage)
      } else {
        android.util.Base64.encodeToString(compressedMessage, 0)
      }
    }

    private fun String.computeSha1Hash(machineLocale: OppiaLocale.MachineLocale): String {
      return machineLocale.run {
        MessageDigest.getInstance("SHA-1")
          .digest(this@computeSha1Hash.toByteArray())
          .joinToString("") { "%02x".formatForMachines(it) }
      }
    }
  }
}
