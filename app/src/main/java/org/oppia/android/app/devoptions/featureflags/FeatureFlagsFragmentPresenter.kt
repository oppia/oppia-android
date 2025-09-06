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
import org.oppia.android.app.databinding.databinding.PendingChangesDialogFragmentBinding
import org.oppia.android.app.devoptions.AppRestartDialogFragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import kotlin.system.exitProcess

/** Tag for displaying [AppRestartDialogFragment]. */
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

  /** Indicates whether the pending changes dialog is open. */
  var isPendingDialogOpen: Boolean = false

  /** Called when [FeatureFlagsFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    featureFlagStates: Map<FeatureFlagId, Boolean>,
    resetFlags: Map<FeatureFlagId, Boolean>,
    isPendingDialogOpen: Boolean
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
      val overriddenFlags = computeOverriddenFlags()
      savePendingFeatureFlags(overriddenFlags)
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

    if (isPendingDialogOpen) {
      onBackNavigation()
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
    val resetFlags = getResetFeatureFlags()

    if (overriddenFlags.isNotEmpty() || resetFlags.isNotEmpty()) {
      showPendingChangesDialog(overriddenFlags)
    } else {
      activity.finish()
    }
  }

  private fun showPendingChangesDialog(overriddenFlags: List<OverriddenFeatureFlag>) {
    isPendingDialogOpen = true
    val dialogBinding = PendingChangesDialogFragmentBinding.inflate(
      LayoutInflater.from(activity),
      /* root= */ null,
      /* attachToRoot= */ false
    )
    val dialog = AlertDialog.Builder(activity)
      .setView(dialogBinding.root)
      .create()

    dialogBinding.saveButton.setOnClickListener {
      isPendingDialogOpen = false
      dialog.dismiss()
      savePendingFeatureFlags(overriddenFlags)
    }

    dialogBinding.discardButton.setOnClickListener {
      dialog.dismiss()
      activity.finish()
    }
    if (!activity.isFinishing && !activity.isDestroyed) {
      dialog.show()
    }
  }

  private fun savePendingFeatureFlags(overriddenFlags: List<OverriddenFeatureFlag>) {
    val resetFlags = getResetFeatureFlags().toList()
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
    val resetFlags = getResetFeatureFlags().keys.toList()

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
              "Failed to reset feature flags: ",
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
            val dialog = AppRestartDialogFragment.newInstance()
            dialog.showNow(fragment.childFragmentManager, TAG_FEATURE_FLAG_RESTART_DIALOG)
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
        model.isChecked.set(state)
      }
    }

    model.onFeatureFlagToggleCallback = { id, newValue ->
      featureFlagsViewModel.updateFeatureFlagState(id, newValue, model.currentValue)
    }
  }

  private fun handleResetFeatureFlag(
    model: FeatureFlagItemViewModel
  ) {
    val restoredFlagValue = model.nonOverriddenValue
    featureFlagsViewModel.updateResetFlag(model.featureFlagId, model.nonOverriddenValue)
    featureFlagsViewModel.updateFeatureFlagState(
      model.featureFlagId,
      restoredFlagValue,
      model.currentValue
    )
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

  /**
   * Returns the feature flags which have been reset.
   *
   * @return a [MutableMap] mapping each [FeatureFlagId] to its boolean reset state,
   *  or an empty map if no reset flags are recorded.
   */
  fun getResetFeatureFlags(): MutableMap<FeatureFlagId, Boolean> {
    return featureFlagsViewModel.resetFlags.value?.toMutableMap() ?: mutableMapOf()
  }

  /**
   * Returns the current states of all feature flags.
   *
   * @return a [Map] mapping each [FeatureFlagId] to its current boolean state,
   *   or an empty map if no feature flag states are recorded.
   */
  fun getFeatureFlagStates(): Map<FeatureFlagId, Boolean> {
    return featureFlagsViewModel.featureFlagStates.value ?: mapOf()
  }

  /**
   * Called when restart is triggered by [AppRestartDialogFragment].
   * Performs a fresh restart of the app to load any updated feature flag states, if required.
   */
  fun restartApp() {
      val intent = Intent(activity, SplashActivity::class.java).also {
        it.action = Intent.ACTION_MAIN
        it.addCategory(Intent.CATEGORY_LAUNCHER)
      }
      activity.finishAffinity()
      activity.startActivity(intent)
      // App is terminated to ensure a fresh restart and kill all the current process
      // so that ProcessState can be reinitialised on the fresh restart.
      exitProcess(0)
  }
}
