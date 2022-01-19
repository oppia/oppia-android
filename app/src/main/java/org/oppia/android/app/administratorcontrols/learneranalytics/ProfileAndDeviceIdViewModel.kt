package org.oppia.android.app.administratorcontrols.learneranalytics

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.SyncStatusManager
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DEFAULT
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NETWORK_ERROR

private const val DEFAULT_COPIED_ID_TEXT = "default_copied_id_text"

@FragmentScope
class ProfileAndDeviceIdViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val oppiaLogger: OppiaLogger,
  private val syncStatusManager: SyncStatusManager,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler,
  context: Context
) : ObservableViewModel() {

  val deviceId =
    resourceHandler.getStringInLocale(R.string.device_id_label) + loggingIdentifierController.deviceId
  val profiles: LiveData<List<Profile>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processGetProfilesResult
    )
  }
  val syncStatus: LiveData<String> by lazy {
    Transformations.map(
      syncStatusManager.getSyncStatus().toLiveData(), ::processSyncStatusResult
    )
  }
  private var currentCopiedId: MutableLiveData<String> = MutableLiveData(DEFAULT_COPIED_ID_TEXT)

  fun setCurrentCopiedId(value: String) {
    if (value != currentCopiedId.value) {
      currentCopiedId.value = value
      notifyChange()
      Log.i("CopiedId", value)
    }
  }

  fun getCurrentCopiedId(): String? {
    return currentCopiedId.value
  }

  fun getObservable(): LiveData<String> {
    return currentCopiedId
  }

  private fun processSyncStatusResult(
    syncStatusResult: AsyncResult<SyncStatusManager.SyncStatus>
  ): String {
    if (syncStatusResult.isFailure()) {
      oppiaLogger.e(
        "ProfileAndDeviceIdViewModel",
        "Failed to retrieve sync status",
        syncStatusResult.getErrorOrNull()!!
      )
    }
    return when (syncStatusResult.getOrDefault(DEFAULT)) {
      DEFAULT -> resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_default)
      DATA_UPLOADING -> resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_data_uploading)
      DATA_UPLOADED -> resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_data_uploaded)
      NETWORK_ERROR -> resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_network_error)
    }
  }

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<Profile> {
    if (profilesResult.isFailure()) {
      oppiaLogger.e(
        "ProfileAndDeviceIdViewModel",
        "Failed to retrieve the list of profiles",
        profilesResult.getErrorOrNull()!!
      )
    }
    val profileList = profilesResult.getOrDefault(emptyList())

    val sortedProfileList = profileList.sortedBy {
      machineLocale.run { it.name.toMachineLowerCase() }
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.isAdmin }

    adminProfile?.let {
      sortedProfileList.remove(adminProfile)
      sortedProfileList.add(0, it)
    }

    return sortedProfileList
  }
}
