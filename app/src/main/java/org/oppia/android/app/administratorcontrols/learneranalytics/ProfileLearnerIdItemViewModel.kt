package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.clipboard.ClipboardController
import org.oppia.android.domain.clipboard.ClipboardController.CurrentClip
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ProfileListItemViewModel] that represents the portion of the learner analytics admin page which
 * shows, and allows the user to copy, the learner ID associated with a specific profile.
 *
 * @property profile the [Profile] associated with this particular model instance
 */
class ProfileLearnerIdItemViewModel private constructor(
  val profile: Profile,
  private val clipboardController: ClipboardController,
  private val resourceHandler: AppLanguageResourceHandler,
  private val analyticsController: AnalyticsController,
  private val oppiaLogger: OppiaLogger,
  private val fragment: Fragment
) : ProfileListItemViewModel(ProfileListViewModel.ProfileListItemViewType.LEARNER_ID) {
  /** The current ID copied to the user's clipboard, or ``null`` if there isn't one. */
  val currentCopiedId: LiveData<String?> by lazy {
    Transformations.map(clipboardController.getCurrentClip().toLiveData(), this::processCurrentClip)
  }

  /** The current [OppiaEventLogs] that have been recorded over the lifetime of the app. */
  val oppiaEventLogs: LiveData<AsyncResult<OppiaEventLogs>> by lazy {
    analyticsController.getEventLogStore().toLiveData()
  }

  /** The current [UploadEventsStats] that represent the current profile's event upload stats. */
  val uploadEventsStats: LiveData<UploadEventsStats> by lazy {
    Transformations.map(oppiaEventLogs, this::processEventLogs)
  }

  /**
   * Copies the analytics learner ID associated with this model's [profile] to the user's clipboard.
   */
  fun copyLearnerId() {
    clipboardController.setCurrentClip(
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.learner_analytics_learner_id_clipboard_label_description, profile.name
      ),
      profile.learnerId
    ).toLiveData().observe(fragment) {
      if (it !is AsyncResult.Success) {
        oppiaLogger.w(
          "ProfileLearnerIdItemViewModel",
          "Encountered unexpected non-successful result when copying to clipboard: $it"
        )
      }
    }
  }

  /**
   * Returns a textual representation of the number of events awaiting upload for the profile
   * corresponding to this view model.
   */
  fun computeEventsWaitingUploadLabelText(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.learner_analytics_learner_events_waiting_upload, profile.name
    )
  }

  /**
   * Returns a textual representation of the number of events that have been uploaded for the
   * profile corresponding to this view model.
   */
  fun computeEventsUploadedLabelText(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.learner_analytics_learner_events_uploaded, profile.name
    )
  }

  /**
   * Stats structure to provide context on the synchronization & uploading information of a specific
   * profile's events.
   *
   * @property learnerStats the stats corresponding to learner-specific events
   * @property uncategorizedStats the stats corresponding to events not tied to an individual
   *     profile, nor ``null`` if these aren't computed for this user
   */
  data class UploadEventsStats(
    val learnerStats: CategorizedEventStats,
    val uncategorizedStats: CategorizedEventStats?
  ) {
    /** Returns whether this stats report includes [uncategorizedStats]. */
    fun hasUncategorizedStats(): Boolean = uncategorizedStats != null

    companion object {
      /**
       * Returns a new [UploadEventsStats] with event stats retrieved from [oppiaEventLogs] for the
       * specified [profile] (and localized per [resourceHandler]).
       *
       * Note that only admin profiles will include [uncategorizedStats] in the returned stats
       * object.
       */
      fun createFrom(
        resourceHandler: AppLanguageResourceHandler,
        profile: Profile,
        oppiaEventLogs: OppiaEventLogs
      ): UploadEventsStats {
        val logsToUploadMap = oppiaEventLogs.eventLogsToUploadList.associateByProfileId()
        val uploadedLogsMap = oppiaEventLogs.uploadedEventLogsList.associateByProfileId()
        return UploadEventsStats(
          learnerStats = CategorizedEventStats.createFrom(
            resourceHandler, profile.id, logsToUploadMap, uploadedLogsMap
          ),
          uncategorizedStats = if (profile.isAdmin) {
            // Admins should also show uncategorized stats.
            CategorizedEventStats.createFrom(
              resourceHandler, profileId = null, logsToUploadMap, uploadedLogsMap
            )
          } else null
        )
      }

      /**
       * Returns a new [UploadEventsStats] with unknown event counts filled in (localized per
       * [resourceHandler]).
       */
      fun createFromUnknown(resourceHandler: AppLanguageResourceHandler): UploadEventsStats {
        val unknownCountText =
          resourceHandler.getStringInLocale(R.string.learner_analytics_unknown_event_count)
        return UploadEventsStats(
          learnerStats = CategorizedEventStats(
            awaitingUploadEventCountText = unknownCountText,
            uploadedEventCountText = unknownCountText
          ),
          uncategorizedStats = null
        )
      }
    }
  }

  /**
   * Represents event counts awaiting & completing upload attempts for a specific category of events
   * (such as being specific to a particular learner, or general for all events not tied to
   * profiles).
   *
   * @property awaitingUploadEventCountText a textual representation of the number of events cached
   *     but not yet uploaded
   * @property uploadedEventCountText a textual representation of the number of events that have
   *     been uploaded to the remote analytics service
   */
  data class CategorizedEventStats(
    val awaitingUploadEventCountText: String,
    val uploadedEventCountText: String
  ) {
    companion object {
      /**
       * Computes a new [CategorizedEventStats] with computed logs to upload/logs that have been
       * uploaded.
       *
       * @param resourceHandler the resource handler with which to localize the count texts
       * @param profileId the profile to which the computed stats correspond, or null if these stats
       *     are profile-agnostic
       * @param logsToUploadMap all events, grouped per-profile, that haven't yet been uploaded
       * @param uploadedLogsMap all events, grouped per-profile, that been uploaded
       * @return the new [CategorizedEventStats] with computed event logs
       */
      fun createFrom(
        resourceHandler: AppLanguageResourceHandler,
        profileId: ProfileId?,
        logsToUploadMap: Map<ProfileId?, List<EventLog>>,
        uploadedLogsMap: Map<ProfileId?, List<EventLog>>
      ): CategorizedEventStats {
        val logsToUpload = logsToUploadMap[profileId] ?: emptyList()
        val uploadedLogs = uploadedLogsMap[profileId] ?: emptyList()
        return CategorizedEventStats(
          awaitingUploadEventCountText = resourceHandler.toHumanReadableString(logsToUpload.size),
          uploadedEventCountText = resourceHandler.toHumanReadableString(uploadedLogs.size)
        )
      }
    }
  }

  private fun processCurrentClip(result: AsyncResult<CurrentClip>): String? {
    return if (result is AsyncResult.Success) {
      when (val clip = result.value) {
        is CurrentClip.SetWithAppText -> clip.text
        CurrentClip.SetWithOtherContent, CurrentClip.Unknown -> null
      }
    } else null
  }

  private fun processEventLogs(result: AsyncResult<OppiaEventLogs>): UploadEventsStats {
    return when (result) {
      is AsyncResult.Pending -> UploadEventsStats.createFromUnknown(resourceHandler)
      is AsyncResult.Success -> UploadEventsStats.createFrom(resourceHandler, profile, result.value)
      is AsyncResult.Failure -> UploadEventsStats.createFromUnknown(resourceHandler).also {
        oppiaLogger.e(
          "ProfileLearnerIdItemViewModel",
          "Encountered unexpected failure when processing event logs",
          result.error
        )
      }
    }
  }

  /** Factory for creating new [ProfileLearnerIdItemViewModel]s. */
  class Factory @Inject constructor(
    private val clipboardController: ClipboardController,
    private val resourceHandler: AppLanguageResourceHandler,
    private val analyticsController: AnalyticsController,
    private val oppiaLogger: OppiaLogger,
    private val fragment: Fragment
  ) {
    /** Returns a new [ProfileLearnerIdItemViewModel] corresponding to the specified [profile]. */
    fun create(profile: Profile): ProfileLearnerIdItemViewModel {
      return ProfileLearnerIdItemViewModel(
        profile, clipboardController, resourceHandler, analyticsController, oppiaLogger, fragment
      )
    }
  }

  private companion object {
    private fun List<EventLog>.associateByProfileId(): Map<ProfileId?, List<EventLog>> =
      groupBy { log -> log.profileId.takeIf { log.hasProfileId() } }
  }
}
