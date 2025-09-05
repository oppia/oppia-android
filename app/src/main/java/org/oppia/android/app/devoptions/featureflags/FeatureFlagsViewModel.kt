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
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  /**
   * LiveData that contains a list of [FeatureFlagItemViewModel] which is used to populate the
   * recyclerview in [FeatureFlagsFragment].
   */
  val featureFlagList: LiveData<List<FeatureFlagItemViewModel>> by lazy {
    Transformations.map(ephemeralFlagsLiveData, ::processFeatureFlagList)
  }

  private val ephemeralFlagsLiveData: LiveData<List<EphemeralFeatureFlag>> by lazy {
    Transformations.map(
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags().toLiveData(),
      ::processEphemeralFlagResult
    )
  }

  /** List of feature flag switch states to be used in the fragment. */
  val featureFlagStates = MutableLiveData<MutableMap<FeatureFlagId, Boolean>>(mutableMapOf())

  /** List of feature flags that have been reset. */
  val resetFlags = MutableLiveData<MutableMap<FeatureFlagId, Boolean>>(mutableMapOf())

  /** Tracks whether the Save button is currently enabled (clickable). */
  val isSaveButtonEnabled: LiveData<Boolean> by lazy {
    Transformations.map(featureFlagStates) { it.isNotEmpty() }
  }

  /**
   * Updates the state of a given feature flag with a new value.
   * This method handles the logic for determining whether to add, update, or remove
   * the flag from the tracked states based on the original value and reset status.
   *
   * @param id the [FeatureFlagId] of the feature flag being updated.
   * @param newValue the new value for the feature flag.
   * @param originalValue the original (non-overridden) value of the feature flag.
   */
  fun updateFeatureFlagState(
    id: FeatureFlagId,
    newValue: Boolean,
    currentValue: Boolean
  ) {
    val currentStates = featureFlagStates.value?.toMutableMap() ?: mutableMapOf()
    val resetFlags = resetFlags.value ?: mutableMapOf()
    if (newValue == currentValue && id !in resetFlags) {
      currentStates.remove(id)
    } else {
      currentStates[id] = newValue
    }
    featureFlagStates.value = currentStates
  }

  /**
   * Updates the list of feature flags that are being reset to their non-overridden values.
   *
   * @param id the [FeatureFlagId] of the feature flag being reset.
   * @param restoredValue the value to restore after reset.
   */
  fun updateResetFlag(id: FeatureFlagId, restoredValue: Boolean) {
    val currentResetFlags = resetFlags.value?.toMutableMap() ?: mutableMapOf()
    currentResetFlags[id] = restoredValue
    resetFlags.value = currentResetFlags
  }

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

  private fun processFeatureFlagList(
    ephemeralFeatureFlags: List<EphemeralFeatureFlag>
  ): List<FeatureFlagItemViewModel> {
    return ephemeralFeatureFlags.map { ephemeralFeatureFlag ->
      FeatureFlagItemViewModel(
        featureFlagId = ephemeralFeatureFlag.id,
        currentValue = ephemeralFeatureFlag.currentValue,
        syncStatus = ephemeralFeatureFlag.syncStatus,
        nonOverriddenValue = ephemeralFeatureFlag.nonOverriddenValue,
        nonOverriddenSyncStatus = ephemeralFeatureFlag.nonOverriddenSyncStatus,
        resetFlags = resetFlags,
        machineLocale = machineLocale,
        resourceHandler = resourceHandler
      )
    }
  }
}
