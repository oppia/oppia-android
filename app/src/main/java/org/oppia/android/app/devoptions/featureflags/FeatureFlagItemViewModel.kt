package org.oppia.android.app.devoptions.featureflags

import androidx.annotation.ColorInt
import androidx.databinding.ObservableField
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale

/** [ViewModel] for displaying a feature flag for the recycler view in [FeatureFlagsFragment]. */
class FeatureFlagItemViewModel(
  val featureFlagId: FeatureFlagId,
  val currentValue: Boolean,
  val syncStatus: SyncStatus,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  /** The current on/off state of the feature flag. Used for UI binding. */
  val isChecked: ObservableField<Boolean> = ObservableField(currentValue)

  /** The display name of the feature flag. */
  val featureFlagDisplayName: ObservableField<String> =
    ObservableField(getFeatureFlagDisplayName(featureFlagId))

  /** The text representing the sync status of the feature flag. */
  val syncStatusDisplayText: ObservableField<String> =
    ObservableField(getSyncStatusText())

  /**
   * Callback to be invoked when the feature flag toggle is changed by the user.
   * Passes the [FeatureFlagId] and the new boolean value.
   */
  var onFeatureFlagToggleCallback: ((FeatureFlagId, Boolean) -> Unit)? = null

  /** The background color associated with the current sync status of the feature flag. */
  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor().toInt()

  /** Called when the feature flag switch is toggled in the UI. */
  fun onToggleFeatureFlagSwitch() {
    val newValue = !(isChecked.get() ?: false)
    isChecked.set(newValue)
    onFeatureFlagToggleCallback?.invoke(featureFlagId, newValue)
  }

  private fun getFeatureFlagDisplayName(id: FeatureFlagId): String {
    return machineLocale.run {
      when (id) {
        FeatureFlagId.UNRECOGNIZED,
        FeatureFlagId.FEATURE_FLAG_ID_UNSPECIFIED -> "Unknown Feature"
        else ->
          id.name.toMachineLowerCase()
            .split("_")
            .joinToString(" ") { it.capitalizeForMachines() }
      }
    }
  }

  private fun getSyncStatusText(): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED ->
        resourceHandler.getStringInLocale(R.string.feature_flag_unknown_sync_status)
      SyncStatus.NOT_SYNCED_FROM_SERVER ->
        resourceHandler.getStringInLocale(R.string.feature_flag_default_sync_status)
      SyncStatus.SYNCED_FROM_SERVER ->
        resourceHandler.getStringInLocale(R.string.feature_flag_server_sync_status)
      SyncStatus.LOCAL_OVERRIDE ->
        resourceHandler.getStringInLocale(R.string.feature_flag_overridden_sync_status)
      else ->
        resourceHandler.getStringInLocale(R.string.feature_flag_unknown_sync_status)
    }
  }

  @ColorInt
  private fun retrieveBackgroundColor(): Long {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED -> 0xFF00645C
      SyncStatus.NOT_SYNCED_FROM_SERVER -> 0xFFBE563C
      SyncStatus.SYNCED_FROM_SERVER -> 0xFF00645C
      SyncStatus.LOCAL_OVERRIDE -> 0xFFEFCF24
      else -> 0xFF00645C
    }
  }
}
