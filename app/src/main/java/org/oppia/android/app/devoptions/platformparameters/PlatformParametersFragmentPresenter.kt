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
import androidx.lifecycle.MutableLiveData
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
  private val resourceHandler: AppLanguageResourceHandler,
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

  /** List of platform parameters that have been reset. */
  var resetParameters: MutableMap<PlatformParameterId, PlatformParameterValue> = mutableMapOf()

  /** List of platform parameter states to be used in the fragment. */
  var platformParameterStates =
    MutableLiveData<MutableMap<PlatformParameterId, PlatformParameterValue?>>(
      mutableMapOf()
    )

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
      this.platformParameterStates =
        MutableLiveData(platformParameterStates.toMutableMap())
    }
    if (resetParameters.isNotEmpty()) {
      this.resetParameters = resetParameters.toMutableMap()
    }

    this.platformParameterStates.observe(fragment) { states ->
      binding.viewModel?.isSaveButtonActive?.set(states.isNotEmpty())
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
    val hasInvalidInput = platformParameterStates.value?.containsValue(null) ?: false

    if (!hasInvalidInput) {
      val overriddenPlatformParameters = platformParameterStates.value
        ?.filter { (id, value) -> resetParameters[id] != value }
        ?.map { (id, value) ->
          OverriddenPlatformParameter.newBuilder()
            .setId(id)
            .setOverriddenValue(value)
            .build()
        }.orEmpty()

      platformParameterControllerDebugImpl.resetPlatformParameters(resetParameters.keys.toList())
        .toLiveData().observe(fragment) {
          when (it) {
            is AsyncResult.Success -> {
              overridePlatformParameters(overriddenPlatformParameters)
            }
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "PlatformParametersFragmentPresenter",
                "Failed to reset platform parameters: ",
                it.error
              )
            }
            is AsyncResult.Pending -> {} // Wait for a result.
          }
        }
    } else {
      AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
        .setTitle(R.string.platform_parameter_invalid_input_alert_dialog_title)
        .setMessage(R.string.platform_parameter_invalid_input_alert_dialog_message)
        .setPositiveButton(
          R.string.platform_parameter_invalid_input_alert_dialog_okay_button
        ) { dialog, _ -> dialog.dismiss() }
        .setCancelable(false)
        .show()
    }
  }

  private fun overridePlatformParameters(
    overriddenPlatformParameters: List<OverriddenPlatformParameter>
  ) {
    platformParameterControllerDebugImpl
      .updateOverriddenPlatformParameters(overriddenPlatformParameters).toLiveData()
      .observe(fragment) {
        when (it) {
          is AsyncResult.Success -> {
            (activity as PlatformParametersActivity).finish()
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "PlatformParametersFragmentPresenter",
              "Failed to reset platform parameters: ",
              it.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun bindPlatformParameterItem(
    binding: PlatformParameterItemBinding,
    model: PlatformParameterItemViewModel
  ) {
    binding.viewModel = model
    setPlatformParameterBackgroundColor(model, binding)

    val editText = binding.platformParameterInputEditText
    val previousWatcher = editText.getTag(R.id.platform_parameter_text_watcher) as? TextWatcher
    previousWatcher?.let { editText.removeTextChangedListener(it) }

    if (resetParameters.containsKey(model.platformParameterId)) {
      model.isParamOverridden.set(true)
      model.isResetButtonActive.set(false)
      model.syncDetails.set(getSyncDetails(model.afterResetSyncStatus))
    }

    binding.resetButton.setOnClickListener {
      handleResetParameter(model, binding)
    }

    if (model.currentValue.hasBoolean()) {
      handleBooleanParameter(model, binding)
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
    binding: PlatformParameterItemBinding
  ) {
    val restoredParameterValue = model.afterResetValue
    resetParameters[model.platformParameterId] = restoredParameterValue
    model.syncDetails.set(getSyncDetails(model.afterResetSyncStatus))

    if (model.currentValue.hasBoolean()) {
      platformParameterStates.value = platformParameterStates.value?.apply {
        this[model.platformParameterId] = restoredParameterValue
      }
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

    model.isResetButtonActive.set(false)
    setPlatformParameterBackgroundColor(model, binding)
  }

  private fun handleTextInputParameter(
    model: PlatformParameterItemViewModel,
    binding: PlatformParameterItemBinding
  ) {
    val paramState = platformParameterStates.value?.get(model.platformParameterId)
    val editText = binding.platformParameterInputEditText
    when {
      model.currentValue.hasInteger() -> {
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        if (platformParameterStates.value?.containsKey(model.platformParameterId) == true &&
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
        val originalValue = if (resetParameters.containsKey(model.platformParameterId)) {
          when {
            model.afterResetValue.hasInteger() -> model.afterResetValue.integer.toString()
            model.afterResetValue.hasString() -> model.afterResetValue.string
            else -> ""
          }
        } else {
          when {
            model.currentValue.hasInteger() -> model.currentValue.integer.toString()
            model.currentValue.hasString() -> model.currentValue.string
            else -> ""
          }
        }

        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
          if (!hasFocus) {
            val currentText = editText.text?.toString().orEmpty()
            if (currentText.isBlank()) {
              editText.setText(originalValue)
              model.inputErrorMsg.set("")
            }
          }
        }

        when {
          model.currentValue.hasInteger() -> {
            if (text == model.currentValue.integer.toString() && id !in resetParameters) {
              platformParameterStates.value = platformParameterStates.value?.apply {
                remove(id)
              }
              model.inputErrorMsg.set("")
            } else {
              val parsed = text.toIntOrNull()
              if (parsed == null) {
                model.inputErrorMsg.set(invalidInputErrorText)
                platformParameterStates.value = platformParameterStates.value?.apply {
                  this[id] = null
                }
              } else {
                model.inputErrorMsg.set("")
                platformParameterStates.value = platformParameterStates.value?.apply {
                  this[id] = PlatformParameterValue.newBuilder().setInteger(parsed).build()
                }
              }
            }
          }
          model.currentValue.hasString() -> {
            if (text == model.currentValue.string && id !in resetParameters) {
              platformParameterStates.value = platformParameterStates.value?.apply {
                remove(id)
              }
              model.inputErrorMsg.set("")
            } else {
              if (text.isBlank()) {
                model.inputErrorMsg.set(invalidInputErrorText)
              } else {
                model.inputErrorMsg.set("")
              }
              platformParameterStates.value = platformParameterStates.value?.apply {
                this[id] = PlatformParameterValue.newBuilder()
                  .setString(text)
                  .build()
              }
            }
          }
        }
        setPlatformParameterBackgroundColor(model, binding)
      }
  }

  private fun handleBooleanParameter(
    model: PlatformParameterItemViewModel,
    binding: PlatformParameterItemBinding
  ) {
    if (platformParameterStates.value?.containsKey(model.platformParameterId) == true) {
      model.isChecked.set(platformParameterStates.value?.get(model.platformParameterId)?.boolean)
    }

    model.onPlatformParameterToggledCallback = { id, value ->
      if (value == model.currentValue.boolean && !resetParameters.containsKey(id)) {
        platformParameterStates.value = platformParameterStates.value?.apply {
          remove(id)
        }
      } else {
        platformParameterStates.value = platformParameterStates.value?.apply {
          this[id] = PlatformParameterValue.newBuilder()
            .setBoolean(value)
            .build()
        }
      }
      setPlatformParameterBackgroundColor(model, binding)
    }
  }

  private fun getSyncDetails(syncStatus: SyncStatus): String {
    return when (syncStatus) {
      SyncStatus.SYNCED_FROM_SERVER -> {
        // TODO(#5345): Replace this placeholder message with the actual server last-synced timestamp when available..
        resourceHandler.getStringInLocale(R.string.platform_parameter_synced_from_server_message)
      }
      else ->
        resourceHandler.getStringInLocale(R.string.platform_parameter_never_synced_message)
    }
  }

  private fun setPlatformParameterBackgroundColor(
    model: PlatformParameterItemViewModel,
    binding: PlatformParameterItemBinding
  ) {
    val isModified = platformParameterStates.value?.containsKey(model.platformParameterId) ?: false

    binding.platformParameterConstraintLayout.setBackgroundColor(
      if (isModified) {
        ContextCompat.getColor(
          fragment.requireContext(),
          R.color.component_color_platform_parameter_modified_background_color
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
}
