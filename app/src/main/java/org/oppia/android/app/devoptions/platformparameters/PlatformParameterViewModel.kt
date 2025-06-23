package org.oppia.android.app.devoptions.platformparameters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ViewModel] for [PlatformParameterFragment]. It populates the recycler view with a list of
 * [PlatformParameterItemViewModel] which in turn display the available feature flags.
 */
@FragmentScope
class PlatformParameterViewModel @Inject constructor(
  private val platformParameterDebugController: PlatformParameterDebugController,
) : ObservableViewModel() {

  private val _platformParameterList = MutableLiveData<List<PlatformParameterItemViewModel>>()

  /**
   * List of [PlatformParameterItemViewModel] used to populate recycler view of [PlatformParameterFragment]
   * to display the available feature flags.
   */
  val platformParameterList: LiveData<List<PlatformParameterItemViewModel>> = _platformParameterList

  init {
    loadPlatformParameters()
  }

  private fun loadPlatformParameters() {
    val dataProvider = platformParameterDebugController.loadEphemeralPlatformParameters()
    dataProvider.toLiveData().observeForever { result ->
      when (result) {
        is AsyncResult.Success ->
          _platformParameterList.value =
            processPlatformParameterList(result.value)
        else -> _platformParameterList.value = listOf()
      }
    }
  }

  private fun processPlatformParameterList(
    ephemeralPlatformParameters: List<EphemeralPlatformParameter>
  ):
    List<PlatformParameterItemViewModel> {

      return ephemeralPlatformParameters.map { ephemeralPlatformParameter ->
        val platformParameterName = getPlatformParameterDisplayName(ephemeralPlatformParameter.id)
        val syncStatusText = getSyncStatusText(ephemeralPlatformParameter.syncStatus)
        val isResetAvailable = canReset(ephemeralPlatformParameter)

        PlatformParameterItemViewModel(
          platformParameterName = platformParameterName,
          syncStatus = syncStatusText,
          isResetAvailable = isResetAvailable,
          currentValue = ephemeralPlatformParameter.currentValue,
          syncStatusBackground = getSyncStatusBackground(syncStatusText)
        )
      }
    }

  fun getPlatformParameterDisplayName(id: PlatformParameterId): String {
    return when (id) {
      PlatformParameterId.SPLASH_SCREEN_WELCOME_MESSAGE -> "Splash Screen Welcome Message"
      PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS ->
        "Sync Up Worker Time Period In Hours"
      PlatformParameterId.CACHE_LATEX_RENDERING -> "Cache Latex Rendering"
      PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_UPLOAD_TIME_INTERVAL_IN_MINUTES ->
        "Performance Metrics Collection Upload Time Interval In Minutes"

      PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_HIGH_FREQUENCY_TIME_INTERVAL_IN_MINUTES ->
        "Performance Metrics Collection High Frequency Time Interval In Minutes"

      PlatformParameterId.PERFORMANCE_METRICS_COLLECTION_LOW_FREQUENCY_TIME_INTERVAL_IN_MINUTES ->
        "Performance Metrics Collection Low Frequency Time Interval In Minutes"

      PlatformParameterId.OPTIONAL_APP_UPDATE_VERSION_CODE -> "Optional App Update Version Code"
      PlatformParameterId.FORCED_APP_UPDATE_VERSION_CODE -> "Forced App Update Version Code"
      PlatformParameterId.LOWEST_SUPPORTED_API_LEVEL -> "Lowest Supported API Level"
      PlatformParameterId.NPS_SURVEY_GRACE_PERIOD_IN_DAYS -> "NPS Survey Grace Period In Days"
      PlatformParameterId.NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES ->
        "NPS Survey Minimum Aggregate Learning Time In A Topic In Minutes"

      else -> "Unknown Platform Parameter"
    }
  }

  private fun getSyncStatusText(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED -> "Unknown"
      SyncStatus.NOT_SYNCED_FROM_SERVER -> "Default"
      SyncStatus.SYNCED_FROM_SERVER -> "Server"
      else -> "Unknown"
    }
  }

  /**
   * Returns the background drawable resource ID corresponding to the provided sync status string.
   *
   * @param syncStatus the string representation of the sync status.
   * @return the drawable resource ID to use as the background for this sync status.
   */
  fun getSyncStatusBackground(syncStatus: String): Int {
    return when (syncStatus) {
      "Server" -> R.drawable.rounded_rec_large_border_radius_green
      "Overriden" -> R.drawable.rounded_rect_large_border_radius_yellow
      "Default" -> R.drawable.rounded_rect_large_border_radius
      else -> R.drawable.rounded_rec_large_border_radius_green
    }
  }

  private fun canReset(ephemeralPlatformParameter: EphemeralPlatformParameter): Boolean {
    // modify this logic when overriding is enabled.
    return (
      ephemeralPlatformParameter.syncStatus != SyncStatus.SYNCED_FROM_SERVER &&
        ephemeralPlatformParameter.syncStatus != SyncStatus.NOT_SYNCED_FROM_SERVER
      )
  }
}
