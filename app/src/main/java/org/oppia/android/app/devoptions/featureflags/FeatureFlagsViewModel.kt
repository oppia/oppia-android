package org.oppia.android.app.devoptions.featureflags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.FeatureFlagId
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
 * [ViewModel] for [FeatureFlagsFragment]. It populates the recycler view with a list of
 * [FeatureFlagItemViewModel] which in turn display the available feature flags.
 */
@FragmentScope
class FeatureFlagsViewModel @Inject constructor(
  private val platformParameterDebugController: PlatformParameterDebugController,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  private val _featureFlagList = MutableLiveData<List<FeatureFlagItemViewModel>>()

  /**
   * List of [FeatureFlagItemViewModel] used to populate recycler view of [FeatureFlagsFragment]
   * to display the available feature flags.
   */
  val featureFlagList: LiveData<List<FeatureFlagItemViewModel>> = _featureFlagList

  init {
    loadFeatureFlags()
  }

  private fun loadFeatureFlags() {
    val dataProvider = platformParameterDebugController.loadEphemeralFeatureFlags()
    dataProvider.toLiveData().observeForever { result ->
      when (result) {
        is AsyncResult.Success -> _featureFlagList.value = processFeatureFlagList(result.value)
        else -> _featureFlagList.value = listOf()
      }
    }
  }

  private fun processFeatureFlagList(ephemeralFeatureFlags: List<EphemeralFeatureFlag>):
    List<FeatureFlagItemViewModel> {

    return ephemeralFeatureFlags.map { ephemeralFeatureFlag ->

      FeatureFlagItemViewModel(
        featureFlagId = ephemeralFeatureFlag.id,
        currentValue = ephemeralFeatureFlag.currentValue,
        syncStatus = ephemeralFeatureFlag.syncStatus,
        machineLocale = machineLocale,
        resourceHandler = resourceHandler
      )
    }
  }
}
