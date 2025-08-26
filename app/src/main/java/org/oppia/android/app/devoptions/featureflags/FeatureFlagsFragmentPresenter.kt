package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagsFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagsItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [FeatureFlagsFragment]. */
@FragmentScope
class FeatureFlagsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val featureFlagsViewModelFactory: FeatureFlagsViewModel.Factory,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler,
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private lateinit var binding: FeatureFlagsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<FeatureFlagItemViewModel>

  /** List of feature flags that have been reset.. */
  var resetFlags: MutableMap<FeatureFlagId, Boolean> = mutableMapOf()

  /** List of feature flag switch states to be used in the fragment. */
  var featureFlagStates = MutableLiveData<MutableMap<FeatureFlagId, Boolean>>(mutableMapOf())

  /** Called when [FeatureFlagsFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    featureFlagStates: Map<FeatureFlagId, Boolean>,
    resetFlags: Map<FeatureFlagId, Boolean>
  ): View {
    binding = FeatureFlagsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.featureFlagsToolbar.setNavigationOnClickListener {
      onBackNavigation()
    }
    binding.saveButton.setOnClickListener {
      onBackNavigation()
    }

    activity.onBackPressedDispatcher.addCallback(
      fragment,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          onBackNavigation()
        }
      }
    )

    if (featureFlagStates.isNotEmpty()) {
      this.featureFlagStates = MutableLiveData(featureFlagStates.toMutableMap())
    }
    if (resetFlags.isNotEmpty()) {
      this.resetFlags = resetFlags.toMutableMap()
    }

    this.featureFlagStates.observe(fragment) { states ->
      binding.viewModel?.isSaveButtonActive?.set(states.isNotEmpty())
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = featureFlagsViewModelFactory.create(resetFlags.keys.toList())
    }
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.featureFlagsRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<FeatureFlagItemViewModel> {
    return singleTypeBuilderFactory.create<FeatureFlagItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = FeatureFlagsItemBinding::inflate,
        setViewModel = this::bindFeatureFlagItem
      )
      .build()
  }

  private fun onBackNavigation() {
    val overriddenFeatureFlags: MutableList<OverriddenFeatureFlag> = mutableListOf()

    featureFlagStates.value?.map { (id, value) ->
      if (resetFlags[id] != value) {
        overriddenFeatureFlags.add(
          OverriddenFeatureFlag.newBuilder()
            .setId(id)
            .setOverriddenValue(value)
            .build()
        )
      }
    }

    platformParameterControllerDebugImpl
      .updateOverriddenFeatureFlags(overriddenFeatureFlags).toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Success -> {
            resetFeatureFlags(resetFlags.keys.toList())
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "FeatureFlagsFragmentPresenter",
              "Failed to override feature flags: ",
              it.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun resetFeatureFlags(flagIds: List<FeatureFlagId>) {
    platformParameterControllerDebugImpl.resetFeatureFlags(flagIds).toLiveData().observe(fragment) {
      when (it) {
        is AsyncResult.Success -> {
          (activity as FeatureFlagsActivity).finish()
        }
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "FeatureFlagsFragmentPresenter",
            "Failed to reset feature flags: ",
            it.error
          )
        }
        is AsyncResult.Pending -> {} // Wait for a result.
      }
    }
  }

  private fun bindFeatureFlagItem(
    binding: FeatureFlagsItemBinding,
    model: FeatureFlagItemViewModel
  ) {
    binding.viewModel = model

    binding.resetButton.setOnClickListener {
      handleResetFeatureFlag(model)
    }
    if (resetFlags.containsKey(model.featureFlagId)) {
      model.isFlagOverridden.set(true)
      model.isResetButtonActive.set(false)
      model.syncDetails.set(getSyncDetails(model.afterResetSyncStatus))
    }

    featureFlagStates.value?.let { states ->
      if (states.containsKey(model.featureFlagId)) {
        model.isChecked.set(states[model.featureFlagId])
      }
    }
    model.onFeatureFlagToggleCallback = { id, value ->
      val currentMap = featureFlagStates.value ?: mutableMapOf()
      if (model.currentValue == value && !resetFlags.containsKey(id)) {
        currentMap.remove(id)
      } else {
        currentMap[id] = value
      }
      featureFlagStates.value = currentMap
    }
  }

  private fun handleResetFeatureFlag(model: FeatureFlagItemViewModel) {
    val restoredFlagValue = model.afterResetValue
    resetFlags[model.featureFlagId] = restoredFlagValue

    val currentMap = featureFlagStates.value ?: mutableMapOf()
    currentMap[model.featureFlagId] = restoredFlagValue
    featureFlagStates.value = currentMap
    model.syncDetails.set(getSyncDetails(model.afterResetSyncStatus))
    model.isChecked.set(restoredFlagValue)
    model.isResetButtonActive.set(false)
  }

  private fun getSyncDetails(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNCED_FROM_SERVER -> {
        // TODO(#5345): Remove this filler message once the server sync logic is implemented.
        resourceHandler.getStringInLocale(R.string.platform_parameter_synced_from_server_message)
      }
      else ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
    }
  }
}
