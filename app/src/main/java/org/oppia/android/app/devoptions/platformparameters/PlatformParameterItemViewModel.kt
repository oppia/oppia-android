package org.oppia.android.app.devoptions.platformparameters

import androidx.annotation.ColorInt
import androidx.databinding.ObservableField
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale

/**
 * [ViewModel] for displaying a platform parameter in the RecyclerView of
 * [PlatformParametersFragment].
 */
class PlatformParameterItemViewModel(
  val platformParameterId: PlatformParameterId,
  val currentValue: PlatformParameterValue,
  val syncStatus: SyncStatus,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  /** The observable boolean value of the parameter, used for switch toggles in UI. */
  val isChecked = ObservableField(currentValue.boolean)

  /** Whether the input field should be visible (i.e., if the value is not a boolean). */
  val isInputVisible = ObservableField(!currentValue.hasBoolean())

  /** The display name of the platform parameter. */
  val platformParameterDisplayText = ObservableField(getPlatformParameterDisplayName())

  /** The sync status text for the platform parameter. */
  val syncStatusDisplayText = ObservableField(getSyncStatusText())

  /** The user-editable value of the platform parameter (if it is a string or integer). */
  val inputValue = ObservableField(
    if (currentValue.hasString()) currentValue.string
    else if (currentValue.hasInteger()) currentValue.integer.toString()
    else ""
  )

  /**
   * Callback invoked when a boolean-type platform parameter is toggled.
   * Passes the parameter ID and the new value.
   */
  var onFeatureFlagToggleCallback: ((PlatformParameterId, Boolean) -> Unit)? = null

  /**
   * Callback invoked when a string/integer-type parameter's input text is changed.
   * Passes the parameter ID and the updated string.
   */
  var onTextChangedCallback: ((PlatformParameterId, String) -> Unit)? = null

  /** The background color of the item based on its sync status. */
  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor().toInt()

  /** Called when the boolean toggle switch is clicked by the user. */
  fun onToggleFeatureFlagSwitch() {
    val newValue = !(isChecked.get() ?: false)
    isChecked.set(newValue)
    onFeatureFlagToggleCallback?.invoke(platformParameterId, isChecked.get()!!)
  }

  private fun getPlatformParameterDisplayName(): String {
    return machineLocale.run {
      when (platformParameterId) {
        PlatformParameterId.UNRECOGNIZED,
        PlatformParameterId.PLATFORM_PARAMETER_ID_UNSPECIFIED -> "Unknown"
        else ->
          platformParameterId.name.toMachineLowerCase()
            .split("_")
            .joinToString(" ") { it.capitalizeForMachines() }
      }
    }
  }

  private fun getSyncStatusText(): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_unknown_sync_status)
      SyncStatus.NOT_SYNCED_FROM_SERVER ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_default_sync_status)
      SyncStatus.SYNCED_FROM_SERVER ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_server_sync_status)
      else ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_unknown_sync_status)
    }
  }

  @ColorInt
  private fun retrieveBackgroundColor(): Long {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED -> 0xFF00645C
      SyncStatus.NOT_SYNCED_FROM_SERVER -> 0xFFBE563C
      SyncStatus.SYNCED_FROM_SERVER -> 0xFF00645C
      else -> 0xFF00645C
    }
  }
}
