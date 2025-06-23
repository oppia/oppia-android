package org.oppia.android.app.devoptions.platformparameters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterDebugController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.locale.OppiaLocale

/**
 * [ViewModel] for [PlatformParametersFragment]. It populates the recycler view with a list of
 * [PlatformParameterItemViewModel] which in turn display the available feature flags.
 */
@FragmentScope
class PlatformParametersViewModel @Inject constructor(
  private val platformParameterDebugController: PlatformParameterDebugController,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  private val _platformParameterList = MutableLiveData<List<PlatformParameterItemViewModel>>()

  /**
   * List of [PlatformParameterItemViewModel] used to populate recycler view of [PlatformParametersFragment]
   * to display the available feature flags.
   */
  val platformParameterList: LiveData<List<PlatformParameterItemViewModel>> = _platformParameterList

  init {
    loadPlatformParameters()
  }

  private fun loadPlatformParameters() {
    val dataProvider = platformParameterDebugController.loadEphemeralPlatformParameters()
    dataProvider.toLiveData().observeForever { result ->
      when (result) {
        is AsyncResult.Success ->
          _platformParameterList.value =
            processPlatformParameterList(result.value)
        else -> _platformParameterList.value = listOf()
      }
    }
  }

  private fun processPlatformParameterList(
    ephemeralPlatformParameters: List<EphemeralPlatformParameter>
  ):
    List<PlatformParameterItemViewModel> {

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
}
