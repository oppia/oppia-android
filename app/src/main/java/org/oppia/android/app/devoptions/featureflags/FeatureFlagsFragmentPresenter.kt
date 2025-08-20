package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagsFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagsItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.app.devoptions.PlatformParameterRestartDialogFragment

const val TAG_FEATURE_FLAG_RESTART_DIALOG = "FEATURE_FLAG_RESTART_DIALOG_TAG"

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
  var featureFlagStates: MutableMap<FeatureFlagId, Boolean> = mutableMapOf()

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

    activity.onBackPressedDispatcher.addCallback(
      fragment,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          onBackNavigation()
        }
      }
    )

    if (featureFlagStates.isNotEmpty()) {
      this.featureFlagStates = featureFlagStates.toMutableMap()
    }
    if (resetFlags.isNotEmpty()) {
      this.resetFlags = resetFlags.toMutableMap()
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
    if(featureFlagStates.isNotEmpty()) {
      val overriddenFeatureFlags: MutableList<OverriddenFeatureFlag> = mutableListOf()
      featureFlagStates.map { (id, value) ->
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
              val dialog = PlatformParameterRestartDialogFragment.newInstance()
              dialog.showNow(activity.supportFragmentManager, TAG_FEATURE_FLAG_RESTART_DIALOG)
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
    else {
      (activity as FeatureFlagsActivity).finish()
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
    }

    if (featureFlagStates.containsKey(model.featureFlagId)) {
      model.isChecked.set(featureFlagStates[model.featureFlagId])
    }
    model.onFeatureFlagToggleCallback = { id, value ->
      if (model.currentValue == value && !resetFlags.containsKey(id)) {
        featureFlagStates.remove(id)
      } else {
        featureFlagStates[id] = value
      }
    }
  }

  private fun handleResetFeatureFlag(
    model: FeatureFlagItemViewModel
  ) {
    platformParameterControllerDebugImpl
      .resetFeatureFlag(model.featureFlagId)
      .toLiveData()
      .observe(fragment) { restoredFlagValue ->
        when (restoredFlagValue) {
          is AsyncResult.Success -> {
            resetFlags[model.featureFlagId] = restoredFlagValue.value
            featureFlagStates[model.featureFlagId] = restoredFlagValue.value
            model.isChecked.set(restoredFlagValue.value)
            model.isResetButtonActive.set(false)
            // TODO(#5345): Remove this filler message once the server sync logic is implemented.
            model.syncDetails.set(
              resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
            )
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "FeatureFlagsFragmentPresenter",
              "Failed to reset feature flag: ${model.featureFlagId}", restoredFlagValue.error
            )
          }
          is AsyncResult.Pending -> {} // No action required
        }
      }
  }
}
