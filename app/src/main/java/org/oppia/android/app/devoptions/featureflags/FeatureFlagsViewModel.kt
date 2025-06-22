package org.oppia.android.app.devoptions.featureflags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.util.locale.OppiaLocale

/**
 * [ViewModel] for [FeatureFlagsFragment]. It populates the recycler view with a list of
 * [FeatureFlagItemViewModel] which in turn display the available feature flags.
 */
@FragmentScope
class FeatureFlagsViewModel @Inject constructor(
  private val platformParameterDebugController: PlatformParameterDebugController,
  private val machineLocale: OppiaLocale.MachineLocale
) : ObservableViewModel() {

  private val _featureFlagList = MutableLiveData<List<FeatureFlagItemViewModel>>()

  /**
   * List of [FeatureFlagItemViewModel] used to populate recycler view of [FeatureFlagsFragment]
   * to display the available feature flags.
   */
  val featureFlagList: LiveData<List<FeatureFlagItemViewModel>> = _featureFlagList

  init {
    loadFeatureFlags()
  }

  private fun loadFeatureFlags() {
    val dataProvider = platformParameterDebugController.loadEphemeralFeatureFlags()
    dataProvider.toLiveData().observeForever { result ->
      when (result) {
        is AsyncResult.Success -> _featureFlagList.value = processFeatureFlagList(result.value)
        else -> _featureFlagList.value = listOf()
      }
    }
  }

  private fun processFeatureFlagList(ephemeralFeatureFlags: List<EphemeralFeatureFlag>):
    List<FeatureFlagItemViewModel> {

      return ephemeralFeatureFlags.map { ephemeralFeatureFlag ->
        val featureFlagName = getFeatureFlagDisplayName(ephemeralFeatureFlag.id)
        val syncStatusText = getSyncStatusText(ephemeralFeatureFlag.syncStatus)

        FeatureFlagItemViewModel(
          featureFlagName = featureFlagName,
          syncStatus = syncStatusText,
          currentValue = ephemeralFeatureFlag.currentValue,
          syncStatusBackground = getSyncStatusBackground(syncStatusText)
        )
      }
    }

  private fun getFeatureFlagDisplayName(
    id: FeatureFlagId
  ): String {
    return machineLocale.run {
      when (id) {
        FeatureFlagId.UNRECOGNIZED,
        FeatureFlagId.FEATURE_FLAG_ID_UNSPECIFIED -> "Unknown Feature"
        else -> id.name.toMachineLowerCase()
          .split("_")
          .joinToString(" ") { it.capitalizeForMachines() }
      }
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
   * @param syncStatus the string representation of the sync status for which to retrieve the background.
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
}
