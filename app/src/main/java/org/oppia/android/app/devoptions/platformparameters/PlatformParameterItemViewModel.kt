package org.oppia.android.app.devoptions.platformparameters

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

  /**
   *  Determines whether to display the parameter as a text input or toggle switch,
   *  based on the parameter type.
   */
  val isTextInputMode = ObservableField(!currentValue.hasBoolean())

  /** The display name of the platform parameter. */
  val platformParameterDisplayName = ObservableField(retrievePlatformParameterDisplayName())

  /** Error message to be displayed in case of invalid input type for a platform parameter. */
  val inputErrorMsg = ObservableField("")

  /** The user-editable value of the platform parameter (if it is a string or integer). */
  val inputValue = ObservableField(
    when {
      currentValue.hasString() -> currentValue.string
      currentValue.hasInteger() -> currentValue.integer.toString()
      else -> ""
    }
  )

  /**
   * Callback invoked when a boolean-type platform parameter is toggled.
   * Passes the parameter ID and the new boolean value.
   */
  var onPlatformParameterToggledCallback: ((PlatformParameterId, Boolean) -> Unit)? = null

  /**
   * Callback invoked when a string/integer-type platform parameter is edited.
   * Passes the parameter ID and the updated string value.
   */
  var onPlatformParameterTextChangedCallback: ((PlatformParameterId, String) -> Unit)? = null

  /** Indicates whether the parameter is overridden. */
  val isParamOverridden: ObservableField<Boolean> =
    ObservableField(syncStatus == SyncStatus.LOCAL_OVERRIDE)

  /** Tracks whether the reset button is currently enabled (clickable). */
  val isResetButtonActive: ObservableField<Boolean> = ObservableField(true)

  /** Message for displaying the sync details of the platform parameter. */
  var syncDetails = ObservableField(processSyncDetails())

  private fun processSyncDetails(): String {
    return when (syncStatus) {
      SyncStatus.LOCAL_OVERRIDE ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_currently_overridden_message)
      SyncStatus.SYNCED_FROM_SERVER -> {
        // TODO(#5345): Remove this filler message once the server sync logic is implemented.
        resourceHandler.getStringInLocale(R.string.platform_parameter_synced_from_server_message)
      }
      else ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
    }
  }

  /** Called when the boolean toggle switch is clicked by the user. */
  fun onTogglePlatformParameterSwitch() {
    val newValue = !(isChecked.get() ?: false)
    isChecked.set(newValue)
    onPlatformParameterToggledCallback?.invoke(platformParameterId, newValue)
  }

  private fun retrievePlatformParameterDisplayName(): String {
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
}
