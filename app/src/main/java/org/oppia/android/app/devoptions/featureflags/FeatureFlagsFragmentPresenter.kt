package org.oppia.android.app.devoptions.featureflags

import android.app.AlertDialog
import android.content.Intent
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
import org.oppia.android.app.databinding.databinding.SaveDiscardDialogFragmentBinding
import org.oppia.android.app.devoptions.PlatformParameterRestartDialogFragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import kotlin.system.exitProcess

/** Tag for displaying [PlatformParameterRestartDialogFragment]. */
const val TAG_FEATURE_FLAG_RESTART_DIALOG = "FEATURE_FLAG_RESTART_DIALOG_TAG"

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
  private var isRestartInitiated: Boolean = false
  private var isSaveButtonClicked: Boolean = false

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
      isSaveButtonClicked = true
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
    val resetFlags = getResetFeatureFlags().toList()

    if (featureFlagStates.value.isNullOrEmpty()) {
      (activity as FeatureFlagsActivity).finish()
      return
    }

    if (!isSaveButtonClicked) {
      val dialogBinding = SaveDiscardDialogFragmentBinding.inflate(
        LayoutInflater.from(activity),
        /* parent= */ null,
        /* attachToRoot= */ false
      )

      val dialog = AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
        .setView(dialogBinding.root)
        .create()
      dialog.setCanceledOnTouchOutside(false)
      dialogBinding.discardButton.setOnClickListener {
        dialog.dismiss()
      }
      dialogBinding.saveButton.setOnClickListener {
        isSaveButtonClicked = true
        dialog.dismiss()
        onBackNavigation()
      }
      dialog.show()
      return
    }
    val overriddenFeatureFlags = featureFlagStates.value
      ?.filter { (id, value) -> resetFlags[id] != value }
    when {
      resetFlags.isNotEmpty() -> applyResetsThenOverrides(overriddenFlags)
      overriddenFlags.isNotEmpty() -> overrideFeatureFlags(overriddenFlags)
      else -> activity.finish()
    }
  }

  private fun computeOverriddenFlags(): List<OverriddenFeatureFlag> {
    return featureFlagsViewModel.featureFlagStates.value
      ?.filter { (id, value) -> featureFlagsViewModel.resetFlags.value?.get(id) != value }
      ?.map { (id, value) ->
        OverriddenFeatureFlag.newBuilder()
          .setId(id)
          .setOverriddenValue(value)
          .build()
      }.orEmpty()
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
              "Failed to reset feature flag: ",
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
            isRestartInitiated = true
            val dialog = PlatformParameterRestartDialogFragment.newInstance()
            dialog.showNow(activity.supportFragmentManager, TAG_FEATURE_FLAG_RESTART_DIALOG)
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

    if (getResetFeatureFlags().containsKey(model.featureFlagId)) {
      model.isFlagOverridden.set(true)
    }

    featureFlagsViewModel.featureFlagStates.value?.let { states ->
      states[model.featureFlagId]?.let { state ->
        model.isFlagOverridden.set(state)
      }
    }

    model.onFeatureFlagToggleCallback = { id, value ->
      if (model.currentValue == value &&
        id !in getResetFeatureFlags()
      ) {
        featureFlagsViewModel.removeFlagState(id)
      } else {
        featureFlagsViewModel.updateFeatureFlagState(id, value)
      }
    }
  }

  private fun handleResetFeatureFlag(
    model: FeatureFlagItemViewModel
  ) {
    val restoredFlagValue = model.nonOverriddenValue
    featureFlagsViewModel.updateResetFlag(model.featureFlagId, model.nonOverriddenValue)
    featureFlagsViewModel.updateFeatureFlagState(model.featureFlagId, restoredFlagValue)
    model.isChecked.set(restoredFlagValue)
  }

  private fun setFeatureFlagBackgroundColor(
    isFlagModified: Boolean,
    model: FeatureFlagItemViewModel
  ): Int {
    return when {
      isFlagModified ->
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

  private fun getResetFeatureFlags(): MutableMap<FeatureFlagId, Boolean> {
    return featureFlagsViewModel.resetFlags.value?.toMutableMap() ?: mutableMapOf()
  }

  /**
   * Called when [FeatureFlagsFragment] is destroyed. Handles app exit if restart is
   * initiated.
   */
  fun handleOnDestroy() {
    if (isRestartInitiated) {
      val intent = Intent(activity, SplashActivity::class.java).also {
        it.action = Intent.ACTION_MAIN
        it.addCategory(Intent.CATEGORY_LAUNCHER)
      }
      activity.startActivity(intent)
      exitProcess(0)
    }
  }
}
