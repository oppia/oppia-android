package org.oppia.android.app.devoptions.platformparameters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/**
 * [ViewModel] for [PlatformParametersFragment]. It populates the recycler view with a list of
 * [PlatformParameterItemViewModel] which in turn display the available Platform Parameters.
 */
@FragmentScope
class PlatformParametersViewModel @Inject constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  /**
   * LiveData that contains a list of [PlatformParameterItemViewModel] which is used to populate the
   * recycler view in [PlatformParametersFragment].
   */
  val platformParameterList: LiveData<List<PlatformParameterItemViewModel>> by lazy {
    Transformations.map(ephemeralParametersLiveData, ::processPlatformParameterList)
  }

  private val ephemeralParametersLiveData: LiveData<List<EphemeralPlatformParameter>> by lazy {
    Transformations.map(
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters().toLiveData(),
      ::processEphemeralParameterResult
    )
  }

  /** List of platform parameter states to be used in the fragment. */
  val platformParameterStates:
    MutableLiveData<MutableMap<PlatformParameterId, PlatformParameterValue?>> =
      MutableLiveData(mutableMapOf())

  /** List of platform parameters that have been reset. */
  val resetParameters: MutableLiveData<MutableMap<PlatformParameterId, PlatformParameterValue>> =
    MutableLiveData(mutableMapOf())

  /** Tracks whether the Save button is currently enabled (clickable). */
  val isSaveButtonEnabled: LiveData<Boolean> by lazy {
    Transformations.map(platformParameterStates) { it.isNotEmpty() }
  }

  /**
   * Updates the state of a given platform parameter with a new value.
   * This method handles the logic for determining whether to add, update, or remove
   * the parameter from the tracked states based on the original value and reset status.
   *
   * @param id the [PlatformParameterId] of the parameter to be updated
   * @param newValue the new [PlatformParameterValue] to assign to this parameter.
   * @param originalValue the original (non-overridden) value of the platform parameter.
   */
  fun updatePlatformParameterState(
    id: PlatformParameterId,
    newValue: PlatformParameterValue?,
    currentValue: PlatformParameterValue
  ) {
    val currentStates = platformParameterStates.value?.toMutableMap() ?: mutableMapOf()
    val resetParams = resetParameters.value ?: emptyMap()

    if (newValue == currentValue && id !in resetParams) {
      currentStates.remove(id)
    } else {
      currentStates[id] = newValue
    }

    platformParameterStates.value = currentStates
  }

  /**
   * Updates the list of parameters that are being reset to their non-overridden values.
   *
   * @param id the [PlatformParameterId] of the parameter being reset.
   * @param newValue the [PlatformParameterValue] to restore after reset.
   */
  fun updateResetParameter(id: PlatformParameterId, newValue: PlatformParameterValue) {
    val currentResets = resetParameters.value ?: mutableMapOf()
    currentResets[id] = newValue
    resetParameters.value = currentResets
  }

  private fun processEphemeralParameterResult(
    result: AsyncResult<List<EphemeralPlatformParameter>>
  ): List<EphemeralPlatformParameter> {
    return when (result) {
      is AsyncResult.Success -> {
        result.value.sortedWith(
          compareByDescending<EphemeralPlatformParameter> {
            it.syncStatus == SyncStatus.LOCAL_OVERRIDE
          }.thenBy { it.id.name }
        )
      }
      else -> emptyList()
    }
  }

  private fun processPlatformParameterList(
    ephemeralPlatformParameters: List<EphemeralPlatformParameter>
  ): List<PlatformParameterItemViewModel> {
    return ephemeralPlatformParameters.map { ephemeralPlatformParameter ->
      PlatformParameterItemViewModel(
        platformParameterId = ephemeralPlatformParameter.id,
        currentValue = ephemeralPlatformParameter.currentValue,
        syncStatus = ephemeralPlatformParameter.syncStatus,
        nonOverriddenValue = ephemeralPlatformParameter.nonOverriddenValue,
        nonOverriddenSyncStatus = ephemeralPlatformParameter.nonOverriddenSyncStatus,
        resetParameters = resetParameters,
        machineLocale = machineLocale,
        resourceHandler = resourceHandler
      )
    }
  }
}
