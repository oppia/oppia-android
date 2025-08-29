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
  private val resourceHandler: AppLanguageResourceHandler,
) : ObservableViewModel() {

  /** List of platform parameters that have been reset. */
  val resetParameters: MutableLiveData<MutableMap<PlatformParameterId, PlatformParameterValue>> =
    MutableLiveData(mutableMapOf())

  /** List of platform parameter states to be used in the fragment. */
  val platformParameterStates:
    MutableLiveData<MutableMap<PlatformParameterId, PlatformParameterValue?>> =
      MutableLiveData(mutableMapOf())

  private val ephemeralParametersLiveData: LiveData<List<EphemeralPlatformParameter>> by lazy {
    Transformations.map(
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters().toLiveData(),
      ::processEphemeralParameterResult
    )
  }

  /**
   * LiveData that contains a list of [PlatformParameterItemViewModel] which is used to populate the
   * recycler view in [PlatformParametersFragment].
   */
  val platformParameterList: LiveData<List<PlatformParameterItemViewModel>> by lazy {
    Transformations.map(ephemeralParametersLiveData, ::processPlatformParameterList)
  }

  /** Tracks whether the Save button is currently enabled (clickable). */
  val isSaveButtonActive: LiveData<Boolean> by lazy {
    Transformations.map(platformParameterStates) { states ->
      states.isNotEmpty()
    }
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
        afterResetValue = ephemeralPlatformParameter.nonOverriddenValue,
        afterResetSyncStatus = ephemeralPlatformParameter.nonOverriddenSyncStatus,
        resetParameters = resetParameters,
        machineLocale = machineLocale,
        resourceHandler = resourceHandler
      )
    }
  }
}
