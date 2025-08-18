package org.oppia.android.app.devoptions.featureflags

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

  /**
   * Callback to be invoked when the feature flag toggle is changed by the user.
   * Passes the [FeatureFlagId] and the new boolean value.
   */
  var onFeatureFlagToggleCallback: ((FeatureFlagId, Boolean) -> Unit)? = null

  /** Indicates whether the flag is overridden. */
  val isFlagOverridden = ObservableField(syncStatus == SyncStatus.LOCAL_OVERRIDE)

  /** Tracks whether the reset button is currently enabled (clickable). */
  val isResetButtonActive = ObservableField(true)

  /** Message for displaying the sync details of the platform parameter. */
  var syncDetails = ObservableField(processSyncDetails())

  private fun processSyncDetails(): String {
    return if (syncStatus == SyncStatus.LOCAL_OVERRIDE)
      resourceHandler
        .getStringInLocale(R.string.platform_parameter_currently_overridden_message)
    else {
      resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
    }
  }

  /** Indicates whether the reset button should be shown for this flag. */
  val isResetAvailable = ObservableField(syncStatus == SyncStatus.LOCAL_OVERRIDE)

  /** Tracks whether the reset button is currently enabled (clickable). */
  val isResetButtonActive = ObservableField(true)

  /** Message for displaying the sync details of the platform parameter. */
  var syncDetails = ObservableField(processSyncDetails())

  private fun processSyncDetails(): String {
    return if (syncStatus == SyncStatus.LOCAL_OVERRIDE)
      resourceHandler
        .getStringInLocale(R.string.platform_parameter_currently_overridden_message)
    else {
      resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
    }
  }

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
}
