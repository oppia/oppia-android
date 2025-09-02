package org.oppia.android.app.devoptions.platformparameters

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.PlatformParameterItemBinding
import org.oppia.android.app.databinding.databinding.PlatformParametersFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OverriddenPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [PlatformParametersFragment]. */
@FragmentScope
class PlatformParametersFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val platformParameterViewModel: PlatformParametersViewModel,
  resourceHandler: AppLanguageResourceHandler,
  private val oppiaLogger: OppiaLogger,
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private lateinit var binding: PlatformParametersFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<PlatformParameterItemViewModel>
  private val invalidInputErrorText =
    resourceHandler.getStringInLocale(R.string.platform_parameter_invalid_input_error_msg)
  private val boundParamIds = mutableSetOf<PlatformParameterId>()

  /** Called when [PlatformParametersFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    platformParameterStates: Map<PlatformParameterId, PlatformParameterValue?>,
    resetParameters: Map<PlatformParameterId, PlatformParameterValue>
  ): View {
    binding = PlatformParametersFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.platformParametersToolbar.setNavigationOnClickListener {
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

    activity.onBackPressedDispatcher.addCallback(
      fragment,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          onBackNavigation()
        }
      }
    )

    if (platformParameterStates.isNotEmpty()) {
      platformParameterViewModel.platformParameterStates.value =
        platformParameterStates.toMutableMap()
    }
    if (resetParameters.isNotEmpty()) {
      platformParameterViewModel.resetParameters.value = resetParameters.toMutableMap()
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)
    bindingAdapter = createRecyclerViewAdapter()
    binding.platformParametersRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = platformParameterViewModel
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<PlatformParameterItemViewModel> {
    return singleTypeBuilderFactory.create<PlatformParameterItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = PlatformParameterItemBinding::inflate,
        setViewModel = this::bindPlatformParameterItem
      )
      .build()
  }

  private fun onBackNavigation() {
    val hasInvalidInput = platformParameterViewModel
      .platformParameterStates.value?.containsValue(null) ?: false

    if (hasInvalidInput) {
      showInvalidInputDialog()
      return
    }

    val overriddenParameters = computeOverriddenParameters()
    val resetParameters = getResetParameters().keys.toList()

    when {
      resetParameters.isNotEmpty() -> applyResetsThenOverrides(overriddenParameters)
      overriddenParameters.isNotEmpty() -> overridePlatformParameters(overriddenParameters)
      else -> activity.finish()
    }
  }

  private fun computeOverriddenParameters(): List<OverriddenPlatformParameter> {
    val resetParamsValue = getResetParameters()

    return platformParameterViewModel.platformParameterStates.value
      ?.filter { (id, value) -> resetParamsValue[id] != value }
      ?.map { (id, value) ->
        OverriddenPlatformParameter.newBuilder()
          .setId(id)
          .setOverriddenValue(value)
          .build()
      }
      .orEmpty()
  }

  private fun applyResetsThenOverrides(overriddenParameters: List<OverriddenPlatformParameter>) {
    val resetParameters = getResetParameters().keys.toList()
    platformParameterControllerDebugImpl
      .resetPlatformParameters(resetParameters)
      .toLiveData()
      .observe(fragment) { result ->
        when (result) {
          is AsyncResult.Success -> {
            overridePlatformParameters(overriddenParameters)
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "PlatformParametersFragmentPresenter",
              "Failed to reset platform parameters: ",
              result.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun overridePlatformParameters(overriddenParameters: List<OverriddenPlatformParameter>) {
    platformParameterControllerDebugImpl
      .updateOverriddenPlatformParameters(overriddenParameters)
      .toLiveData()
      .observe(fragment) { result ->
        when (result) {
          is AsyncResult.Success -> {
            activity.finish()
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "PlatformParametersFragmentPresenter",
              "Failed to override platform parameters: ",
              result.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun showInvalidInputDialog() {
    AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(R.string.platform_parameter_invalid_input_alert_dialog_title)
      .setMessage(R.string.platform_parameter_invalid_input_alert_dialog_message)
      .setPositiveButton(
        R.string.platform_parameter_invalid_input_alert_dialog_okay_button
      ) { dialog, _ -> dialog.dismiss() }
      .setCancelable(false)
      .show()
  }

  private fun bindPlatformParameterItem(
    binding: PlatformParameterItemBinding,
    model: PlatformParameterItemViewModel
  ) {
    binding.viewModel = model
    val editText = binding.platformParameterInputEditText
    val previousWatcher = editText.getTag(R.id.platform_parameter_text_watcher) as? TextWatcher
    previousWatcher?.let { editText.removeTextChangedListener(it) }

    platformParameterViewModel.platformParameterStates.observe(fragment) {
      binding.platformParameterConstraintLayout.setBackgroundColor(
        setPlatformParameterBackgroundColor(
          it.containsKey(model.platformParameterId),
          model
        )
      )
    }

    val resetParamsValue = getResetParameters()
    if (resetParamsValue.containsKey(model.platformParameterId)) {
      model.isParamOverridden.set(true)
    }

    binding.resetButton.setOnClickListener {
      handleResetParameter(model)
    }

    if (model.currentValue.hasBoolean()) {
      handleBooleanParameter(model)
    } else {
      handleTextInputParameter(model, binding)
    }

    val newWatcher = object : TextWatcher {
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        model.onPlatformParameterTextChangedCallback?.invoke(
          model.platformParameterId, s.toString()
        )
      }
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun afterTextChanged(s: Editable?) {}
    }

    editText.addTextChangedListener(newWatcher)
    editText.setTag(R.id.platform_parameter_text_watcher, newWatcher)
  }

  private fun handleResetParameter(
    model: PlatformParameterItemViewModel,
  ) {
    val restoredParameterValue = model.afterResetValue
    platformParameterViewModel
      .updateResetParameter(model.platformParameterId, restoredParameterValue)

    if (model.currentValue.hasBoolean()) {
      platformParameterViewModel
        .updatePlatformParameterState(model.platformParameterId, restoredParameterValue)
      model.isChecked.set(restoredParameterValue.boolean)
    } else {
      when {
        model.currentValue.hasInteger() -> {
          model.inputValue.set(restoredParameterValue.integer.toString())
        }

        model.currentValue.hasString() -> {
          model.inputValue.set(restoredParameterValue.string)
        }
      }
    }
  }

  private fun handleTextInputParameter(
    model: PlatformParameterItemViewModel,
    binding: PlatformParameterItemBinding
  ) {
    val paramState = getPlatformParameterStates()[model.platformParameterId]
    val editText = binding.platformParameterInputEditText
    when {
      model.currentValue.hasInteger() -> {
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        if (getPlatformParameterStates().containsKey(model.platformParameterId) &&
          paramState != null
        ) {
          model.inputErrorMsg.set("")
          model.inputValue.set(paramState.integer.toString())
        } else {
          model.inputErrorMsg.set("")
          model.inputValue.set(model.currentValue.integer.toString())
        }
      }
      model.currentValue.hasString() -> {
        editText.inputType = InputType.TYPE_CLASS_TEXT
        model.inputValue.set(paramState?.string ?: model.currentValue.string)
        model.inputErrorMsg.set("")
      }
    }

    boundParamIds.add(model.platformParameterId)

    model.onPlatformParameterTextChangedCallback =
      onPlatformParameterTextChangedCallback@{ id, text ->
        if (boundParamIds.contains(id).not()) {
          return@onPlatformParameterTextChangedCallback
        }
        val resetParamsValue = getResetParameters()
        val lastValidValue = getLastValidValue(model)
        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
          if (!hasFocus) {
            val currentText = editText.text?.toString().orEmpty()
            if (currentText.isBlank()) {
              editText.setText(lastValidValue)
              model.inputErrorMsg.set("")
            }
          }
        }

        when {
          model.currentValue.hasInteger() -> {
            if (text == model.currentValue.integer.toString() && id !in resetParamsValue) {
              platformParameterViewModel.removeParameterState(id)
              model.inputErrorMsg.set("")
            } else {
              val parsed = text.toIntOrNull()
              if (parsed == null) {
                model.inputErrorMsg.set(invalidInputErrorText)
                platformParameterViewModel.updatePlatformParameterState(id, null)
              } else {
                model.inputErrorMsg.set("")
                val parameter = PlatformParameterValue.newBuilder().setInteger(parsed).build()
                platformParameterViewModel.updatePlatformParameterState(id, parameter)
              }
            }
          }
          model.currentValue.hasString() -> {
            if (text == model.currentValue.string && id !in resetParamsValue) {
              platformParameterViewModel.removeParameterState(id)
              model.inputErrorMsg.set("")
            } else {
              if (text.isBlank()) {
                model.inputErrorMsg.set(invalidInputErrorText)
              } else {
                model.inputErrorMsg.set("")
              }
              val parameter = PlatformParameterValue.newBuilder()
                .setString(text)
                .build()
              platformParameterViewModel.updatePlatformParameterState(id, parameter)
            }
          }
        }
      }
  }

  private fun handleBooleanParameter(
    model: PlatformParameterItemViewModel
  ) {

    getPlatformParameterStates()[model.platformParameterId]?.let { state ->
      model.isChecked.set(state.boolean)
    }

    model.onPlatformParameterToggledCallback = { id, value ->
      val resetParamsValue = getResetParameters()
      if (value == model.currentValue.boolean && !resetParamsValue.containsKey(id)) {
        platformParameterViewModel.removeParameterState(id)
      } else {
        val parameter = PlatformParameterValue.newBuilder()
          .setBoolean(value)
          .build()
        platformParameterViewModel.updatePlatformParameterState(id, parameter)
      }
    }
  }

  private fun getLastValidValue(model: PlatformParameterItemViewModel): String {
    val resetParamsValue = getResetParameters()
    val value = if (resetParamsValue.containsKey(model.platformParameterId)) {
      model.afterResetValue
    } else {
      model.currentValue
    }
    return when {
      value.hasInteger() -> value.integer.toString()
      value.hasString() -> value.string
      else -> ""
    }
  }

  private fun setPlatformParameterBackgroundColor(
    isModified: Boolean,
    model: PlatformParameterItemViewModel
  ): Int {
    return when {
      isModified ->
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_platform_parameter_modified_background_color
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

  private fun getPlatformParameterStates():
    MutableMap<PlatformParameterId, PlatformParameterValue?> {
      return platformParameterViewModel.platformParameterStates.value ?: mutableMapOf()
    }

  private fun getResetParameters(): MutableMap<PlatformParameterId, PlatformParameterValue> {
    return platformParameterViewModel.resetParameters.value ?: mutableMapOf()
  }
}
