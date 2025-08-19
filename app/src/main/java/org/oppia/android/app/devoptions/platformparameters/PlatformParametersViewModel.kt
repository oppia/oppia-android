package org.oppia.android.app.devoptions.platformparameters

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
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
class PlatformParametersViewModel private constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler,
  private val resetParamList: List<PlatformParameterId>
) : ObservableViewModel() {
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

  private fun processEphemeralParameterResult(
    result: AsyncResult<List<EphemeralPlatformParameter>>
  ): List<EphemeralPlatformParameter> {
    return when (result) {
      is AsyncResult.Success -> {
        result.value.sortedWith(
          compareByDescending<EphemeralPlatformParameter> {
            it.syncStatus == SyncStatus.LOCAL_OVERRIDE || resetParamList.contains(it.id)
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
        machineLocale = machineLocale,
        resourceHandler = resourceHandler
      )
    }
  }

  class Factory @Inject constructor(
    private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
    private val machineLocale: OppiaLocale.MachineLocale,
    private val resourceHandler: AppLanguageResourceHandler
  ) {
    /** Creates a new [PlatformParametersViewModel]. */
    fun create(resetParamList: List<PlatformParameterId>): PlatformParametersViewModel {
      return PlatformParametersViewModel(
        platformParameterControllerDebugImpl,
        machineLocale,
        resourceHandler,
        resetParamList
      )
    }
  }
}
