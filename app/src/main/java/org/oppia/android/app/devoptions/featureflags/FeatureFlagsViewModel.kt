package org.oppia.android.app.devoptions.featureflags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/**
 * [ViewModel] for [FeatureFlagsFragment]. It populates the recycler view with a list of
 * [FeatureFlagItemViewModel] which in turn display the available feature flags.
 */
@FragmentScope
class FeatureFlagsViewModel @Inject constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler,
) : ObservableViewModel() {
  private val ephemeralFlagsLiveData: LiveData<List<EphemeralFeatureFlag>> by lazy {
    Transformations.map(
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags().toLiveData(),
      ::processEphemeralFlagResult
    )
  }

  /**
   * LiveData that contains a list of [FeatureFlagItemViewModel] which is used to populate the
   * recycler view in [FeatureFlagsFragment].
   */
  val featureFlagList: LiveData<List<FeatureFlagItemViewModel>> by lazy {
    Transformations.map(ephemeralFlagsLiveData, ::processFeatureFlagList)
  }

  /** Tracks whether the Save button is currently enabled (clickable). */
  val isSaveButtonActive: LiveData<Boolean> by lazy {
    Transformations.map(featureFlagStates) { states ->
      states.isNotEmpty()
    }
  }

  /** List of feature flag switch states to be used in the fragment. */
  var featureFlagStates = MutableLiveData<MutableMap<FeatureFlagId, Boolean>>(mutableMapOf())

  /** List of feature flags that have been reset. */
  var resetFlags = MutableLiveData<MutableMap<FeatureFlagId, Boolean>>(mutableMapOf())

  private fun processEphemeralFlagResult(
    result: AsyncResult<List<EphemeralFeatureFlag>>
  ): List<EphemeralFeatureFlag> {
    return when (result) {
      is AsyncResult.Success -> {
        result.value.sortedWith(
          compareByDescending<EphemeralFeatureFlag> {
            it.syncStatus == SyncStatus.LOCAL_OVERRIDE
          }.thenBy { it.id.name }
        )
      }
      else -> emptyList()
    }
  }

  private fun processFeatureFlagList(ephemeralFeatureFlags: List<EphemeralFeatureFlag>):
    List<FeatureFlagItemViewModel> {
      return ephemeralFeatureFlags.map { ephemeralFeatureFlag ->
        FeatureFlagItemViewModel(
          featureFlagId = ephemeralFeatureFlag.id,
          currentValue = ephemeralFeatureFlag.currentValue,
          syncStatus = ephemeralFeatureFlag.syncStatus,
          afterResetValue = ephemeralFeatureFlag.nonOverriddenValue,
          afterResetSyncStatus = ephemeralFeatureFlag.nonOverriddenSyncStatus,
          resetFlags = resetFlags,
          machineLocale = machineLocale,
          resourceHandler = resourceHandler
        )
      }
    }
}
