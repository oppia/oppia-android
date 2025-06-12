package org.oppia.android.app.devoptions.featureflags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

@FragmentScope
class FeatureFlagViewModel @Inject constructor(
  private val platformParameterDebugController: PlatformParameterDebugController
) : ObservableViewModel() {

  private val _featureFlagList = MutableLiveData<List<FeatureFlagItemViewModel>>()
  val featureFlagList: LiveData<List<FeatureFlagItemViewModel>> = _featureFlagList

  init {
    loadFeatureFlags()
  }

  private fun loadFeatureFlags() {
    val dataProvider = platformParameterDebugController.loadEphemeralFeatureFlags()
    dataProvider.toLiveData().observeForever { result ->
      when (result) {
        is AsyncResult.Success ->
          _featureFlagList.value = processFeatureFlagList(result.value)

        else -> {
          _featureFlagList.value = listOf()
        }
      }
    }
  }
}

private fun processFeatureFlagList(ephemeralFeatureFlags: List<EphemeralFeatureFlag>):
  List<FeatureFlagItemViewModel> {
    val list = mutableListOf<FeatureFlagItemViewModel>()
    ephemeralFeatureFlags.forEach { ephemeralFeatureFlag ->
      val featureFlagName = formatFeatureFlagName(ephemeralFeatureFlag.id.name)
      val syncStatusText = getSyncStatusText(ephemeralFeatureFlag.syncStatus)
      val isResetAvailable = canReset(ephemeralFeatureFlag)

      list.add(
        FeatureFlagItemViewModel(
          featureFlagName = featureFlagName,
          syncStatus = syncStatusText,
          isResetAvailable = isResetAvailable,
          currentValue = ephemeralFeatureFlag.currentValue,
          syncStatusBackground = getSyncStatusBackground(syncStatusText)
        )
      )
    }
    return list
  }

private fun formatFeatureFlagName(flagId: String): String {
  return flagId.replace("_", " ").split(" ").joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { it.uppercase() }
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

fun getSyncStatusBackground(syncStatus: String): Int {
  return when (syncStatus) {
    "Server" -> org.oppia.android.app.R.drawable.rounded_rec_large_border_radius_green
    "Overriden" -> org.oppia.android.app.R.drawable.rounded_rect_large_border_radius_yellow
    "Default" -> org.oppia.android.app.R.drawable.rounded_rect_large_border_radius
    else
    -> org.oppia.android.app.R.drawable.rounded_rec_large_border_radius_green
  }
}

private fun canReset(ephemeralFeatureFlag: EphemeralFeatureFlag): Boolean {
  // modify this logic when overriding is enabled.
  return (
    ephemeralFeatureFlag.syncStatus != SyncStatus.SYNCED_FROM_SERVER &&
      ephemeralFeatureFlag.syncStatus != SyncStatus.NOT_SYNCED_FROM_SERVER
    )
}
