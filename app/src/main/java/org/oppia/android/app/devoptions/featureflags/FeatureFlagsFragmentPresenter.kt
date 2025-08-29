package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagsFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagsItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.recyclerview.BindableAdapter
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
  private val oppiaLogger: OppiaLogger,
  private val featureFlagsViewModel: FeatureFlagsViewModel,
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private lateinit var binding: FeatureFlagsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<FeatureFlagItemViewModel>

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
      featureFlagsViewModel.featureFlagStates.value = featureFlagStates.toMutableMap()
    }
    if (resetFlags.isNotEmpty()) {
      featureFlagsViewModel.resetFlags.value = resetFlags.toMutableMap()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = featureFlagsViewModel
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
    val overriddenFlags = computeOverriddenFlags()
    val resetFlags = featureFlagsViewModel.resetFlags.value?.keys?.toList().orEmpty()

    when {
      resetFlags.isNotEmpty() -> applyResetsThenOverrides(overriddenFlags)
      overriddenFlags.isNotEmpty() -> overrideFeatureFlags(overriddenFlags)
      else -> activity.finish()
    }
  }

  private fun computeOverriddenFlags(): List<OverriddenFeatureFlag> {
    return featureFlagsViewModel.featureFlagStates.value
      ?.filter { (id, value) ->
        featureFlagsViewModel.resetFlags.value?.get(id) != value
      }
      ?.map { (id, value) ->
        OverriddenFeatureFlag.newBuilder()
          .setId(id)
          .setOverriddenValue(value)
          .build()
      }
      .orEmpty()
  }

  private fun applyResetsThenOverrides(overriddenFlags: List<OverriddenFeatureFlag>) {
    val resetFlags = featureFlagsViewModel.resetFlags.value?.keys?.toList().orEmpty()

    platformParameterControllerDebugImpl
      .resetFeatureFlags(resetFlags)
      .toLiveData()
      .observe(fragment) { result ->
        when (result) {
          is AsyncResult.Success -> {
            overrideFeatureFlags(overriddenFlags)
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "FeatureFlagsFragmentPresenter",
              "Failed to reset platform parameters: ",
              result.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun overrideFeatureFlags(overriddenFlags: List<OverriddenFeatureFlag>) {
    platformParameterControllerDebugImpl
      .updateOverriddenFeatureFlags(overriddenFlags)
      .toLiveData()
      .observe(fragment) { result ->
        when (result) {
          is AsyncResult.Success -> {
            activity.finish()
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "FeatureFlagsFragmentPresenter",
              "Failed to override feature flags: ",
              result.error
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
    featureFlagsViewModel.featureFlagStates.observe(fragment) {
      binding.featureFlagConstraintLayout.setBackgroundColor(
        setFeatureFlagBackgroundColor(it.containsKey(model.featureFlagId), model)
      )
    }

    if (featureFlagsViewModel.resetFlags.value?.containsKey(model.featureFlagId) == true) {
      model.isFlagOverridden.set(true)
    }

    featureFlagsViewModel.featureFlagStates.value?.let { states ->
      if (states.containsKey(model.featureFlagId)) {
        model.isChecked.set(states[model.featureFlagId])
      }
    }

    model.onFeatureFlagToggleCallback = { id, value ->
      val currentMap = featureFlagsViewModel.featureFlagStates.value ?: mutableMapOf()
      if (model.currentValue == value &&
        id !in (featureFlagsViewModel.resetFlags.value ?: emptyMap())
      ) {
        currentMap.remove(id)
      } else {
        currentMap[id] = value
      }
      featureFlagsViewModel.featureFlagStates.value = currentMap
    }
  }

  private fun handleResetFeatureFlag(
    model: FeatureFlagItemViewModel
  ) {
    val restoredFlagValue = model.afterResetValue
    val resetMap = featureFlagsViewModel.resetFlags.value ?: mutableMapOf()
    resetMap[model.featureFlagId] = restoredFlagValue
    featureFlagsViewModel.resetFlags.value = resetMap

    val currentMap = featureFlagsViewModel.featureFlagStates.value ?: mutableMapOf()
    currentMap[model.featureFlagId] = restoredFlagValue
    featureFlagsViewModel.featureFlagStates.value = currentMap
    model.isChecked.set(restoredFlagValue)
  }

  private fun setFeatureFlagBackgroundColor(
    isModified: Boolean,
    model: FeatureFlagItemViewModel
  ): Int {
    return when {
      isModified ->
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_feature_flag_modified_background_color
        )
      model.syncStatus == SyncStatus.LOCAL_OVERRIDE ->
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_platform_parameter_overridden_background_color
        )
      else ->
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_shared_item_background_solid_color
        )
    }
  }
}
