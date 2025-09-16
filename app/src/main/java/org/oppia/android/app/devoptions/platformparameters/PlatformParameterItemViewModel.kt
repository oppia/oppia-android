package org.oppia.android.app.devoptions.platformparameters

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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
  val nonOverriddenValue: PlatformParameterValue,
  val nonOverriddenSyncStatus: SyncStatus,
  val resetParameters: MutableLiveData<MutableMap<PlatformParameterId, PlatformParameterValue>>,
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

  /** Indicates whether this platform parameter has been overridden locally. */
  val isParamOverridden: ObservableField<Boolean> =
    ObservableField(syncStatus == SyncStatus.LOCAL_OVERRIDE)

  /** Tracks whether the reset button is currently enabled (clickable). */
  val isResetButtonEnabled: LiveData<Boolean> by lazy {
    Transformations.map(resetParameters) { platformParameterId !in it }
  }

  /** Represents the platform parameter’s server-sync or override state. */
  val syncDetails: LiveData<String> = Transformations.map(resetParameters, ::processSyncDetails)

  private fun processSyncDetails(
    resetParameters: MutableMap<PlatformParameterId, PlatformParameterValue>
  ): String {
    return when {
      resetParameters.containsKey(platformParameterId) -> getSyncDetails(nonOverriddenSyncStatus)
      else -> getSyncDetails(syncStatus)
    }
  }

  private fun getSyncDetails(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.LOCAL_OVERRIDE ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_currently_overridden_message)
      SyncStatus.SYNCED_FROM_SERVER ->
        // TODO(#5951): Replace this placeholder message with the actual server last-synced timestamps.
        resourceHandler.getStringInLocale(R.string.platform_parameter_synced_from_server_message)
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
