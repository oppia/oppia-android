// FeatureFlagViewModel.kt
package org.oppia.android.app.devoptions.featureflags

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

@FragmentScope
class FeatureFlagViewModel @Inject constructor(
  private val platformParameterDebugController: PlatformParameterDebugController)
  : ObservableViewModel() {

  val isButtonActive = ObservableField(true)

  private val _featureFlagList = MutableLiveData<List<FeatureFlagItemViewModel>>()
  val featureFlagList: LiveData<List<FeatureFlagItemViewModel>> = _featureFlagList

  init {
    loadFeatureFlags()
  }

  private fun loadFeatureFlags() {
    val dataProvider = platformParameterDebugController.loadEphemeralFeatureFlags()
    dataProvider.toLiveData().observeForever { result ->
      _featureFlagList.value = processFeatureFlagList(result.value)
    }
  }
}

private fun processFeatureFlagList(ephemeralFeatureFlags: List<EphemeralFeatureFlag>): List<FeatureFlagItemViewModel> {
  val list = mutableListOf<FeatureFlagItemViewModel>()
  ephemeralFeatureFlags.forEach { ephemeralFeatureFlag ->
    val featureFlagName = formatFeatureFlagName(ephemeralFeatureFlag.id.name)
    val syncStatusText = getSyncStatusText(ephemeralFeatureFlag.syncStatus)
    val isResetAvailable = canReset(ephemeralFeatureFlag)

    list.add(FeatureFlagItemViewModel(
      featureFlagName = featureFlagName,
      syncStatus = syncStatusText,
      isResetAvailable = isResetAvailable,
      currentValue = ObservableField(ephemeralFeatureFlag.currentValue),
    ))
  }
  return list
}

private fun formatFeatureFlagName(flagId: String): String {
  // Convert "DOWNLOADS_SUPPORT" to "Downloads Support"
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

private fun canReset(ephemeralFeatureFlag: EphemeralFeatureFlag): Boolean {
  // modify this logic when overriding is enabled.
  return (ephemeralFeatureFlag.syncStatus != SyncStatus.SYNCED_FROM_SERVER &&
    ephemeralFeatureFlag.syncStatus != SyncStatus.SYNCED_FROM_SERVER)
  }
}