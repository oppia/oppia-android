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
import androidx.lifecycle.MutableLiveData
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
  private val resourceHandler: AppLanguageResourceHandler,
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private lateinit var binding: FeatureFlagsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<FeatureFlagItemViewModel>
  private var isRestartInitiated: Boolean = false
  private var isSaveButtonClicked: Boolean = false

  /** List of feature flags that have been reset. */
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
      ?.map { (id, value) ->
        OverriddenFeatureFlag.newBuilder()
          .setId(id)
          .setOverriddenValue(value)
          .build()
      }
      .orEmpty()

    platformParameterControllerDebugImpl.resetFeatureFlags(resetFlags.keys.toList())
      .toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Success -> {
            overrideFeatureFlags(overriddenFeatureFlags)
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "FeatureFlagsFragmentPresenter",
              "Failed to reset platform parameters: ",
              it.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun overrideFeatureFlags(overriddenFeatureFlags: List<OverriddenFeatureFlag>) {
    platformParameterControllerDebugImpl.updateOverriddenFeatureFlags(overriddenFeatureFlags)
      .toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Success -> {
            isRestartInitiated = true
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

  private fun bindFeatureFlagItem(
    binding: FeatureFlagsItemBinding,
    model: FeatureFlagItemViewModel
  ) {
    binding.viewModel = model

    binding.resetButton.setOnClickListener {
      handleResetFeatureFlag(model, binding)
    }
    setFeatureFlagBackgroundColor(model, binding)

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
      if (model.currentValue == value && id !in resetFlags) {
        currentMap.remove(id)
      } else {
        currentMap[id] = value
      }
      featureFlagStates.value = currentMap
      setFeatureFlagBackgroundColor(model, binding)
    }
  }

  private fun handleResetFeatureFlag(
    model: FeatureFlagItemViewModel,
    binding: FeatureFlagsItemBinding
  ) {
    val restoredFlagValue = model.afterResetValue
    resetFlags[model.featureFlagId] = restoredFlagValue

    val currentMap = featureFlagStates.value ?: mutableMapOf()
    currentMap[model.featureFlagId] = restoredFlagValue
    featureFlagStates.value = currentMap
    model.syncDetails.set(getSyncDetails(model.afterResetSyncStatus))
    model.isChecked.set(restoredFlagValue)
    model.isResetButtonActive.set(false)
    setFeatureFlagBackgroundColor(model, binding)
  }

  private fun getSyncDetails(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNCED_FROM_SERVER -> {
        // TODO(#5345): Replace this placeholder message with the actual server last-synced timestamp when available.
        resourceHandler.getStringInLocale(R.string.platform_parameter_synced_from_server_message)
      }
      else ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
    }
  }

  private fun setFeatureFlagBackgroundColor(
    model: FeatureFlagItemViewModel,
    binding: FeatureFlagsItemBinding
  ) {
    val isModified = featureFlagStates.value?.containsKey(model.featureFlagId) ?: false

    binding.featureFlagConstraintLayout.setBackgroundColor(
      if (isModified) {
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_feature_flag_modified_background_color
        )
      } else {
        if (model.syncStatus == SyncStatus.LOCAL_OVERRIDE) {
          ContextCompat.getColor(
            fragment.requireContext(),
            R.color.component_color_platform_parameter_overridden_background_color
          )
        } else {
          ContextCompat.getColor(
            fragment.requireContext(),
            R.color.component_color_shared_item_background_solid_color
          )
        }
      }
    )
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
