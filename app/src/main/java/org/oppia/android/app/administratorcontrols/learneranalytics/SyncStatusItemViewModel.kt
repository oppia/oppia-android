package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.SyncStatusManager
import javax.inject.Inject

/**
 * [ProfileListItemViewModel] that represents the portion of the learner analytics admin page which
 * shows the current learner analytics event synchronization status.
 */
class SyncStatusItemViewModel private constructor(
  private val syncStatusManager: SyncStatusManager,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler
) : ProfileListItemViewModel(ProfileListViewModel.ProfileListItemViewType.SYNC_STATUS) {
  /** The current analytics event synchronization status, as a human-readable string. */
  val syncStatus: LiveData<String> by lazy {
    Transformations.map(
      syncStatusManager.getSyncStatus().toLiveData(), ::processSyncStatusResult
    )
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
    return when (syncStatusResult.getOrDefault(SyncStatusManager.SyncStatus.INITIAL_UNKNOWN)) {
      SyncStatusManager.SyncStatus.INITIAL_UNKNOWN ->
        resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_default)
      SyncStatusManager.SyncStatus.DATA_UPLOADING ->
        resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_data_uploading)
      SyncStatusManager.SyncStatus.DATA_UPLOADED ->
        resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_data_uploaded)
      SyncStatusManager.SyncStatus.NETWORK_ERROR ->
        resourceHandler.getStringInLocale(R.string.learner_analytics_sync_status_network_error)
    }
  }

  /** Factory for creating new [SyncStatusItemViewModel]s. */
  class Factory @Inject constructor(
    private val syncStatusManager: SyncStatusManager,
    private val oppiaLogger: OppiaLogger,
    private val resourceHandler: AppLanguageResourceHandler
  ) {
    /** Returns a new [SyncStatusItemViewModel]. */
    fun create(): SyncStatusItemViewModel =
      SyncStatusItemViewModel(syncStatusManager, oppiaLogger, resourceHandler)
  }
}
