package org.oppia.android.app.devoptions.platformparameters

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import org.oppia.android.app.databinding.databinding.PlatformParameterItemBinding
import org.oppia.android.app.databinding.databinding.PlatformParametersFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OverriddenPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
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
  private val platformParametersViewModel: PlatformParametersViewModel,
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

  /** List of platform parameter states to be used in the fragment. */
  var platformParameterStates:
    MutableMap<PlatformParameterId, PlatformParameterValue> = mutableMapOf()

  /** Called when [PlatformParametersFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    platformParameterStates: Map<PlatformParameterId, PlatformParameterValue>
  ): View {
    binding = PlatformParametersFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.platformParametersToolbar.setNavigationOnClickListener {
      onBackNavigation()
    }

    activity.onBackPressedDispatcher.addCallback(
      fragment,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          onBackNavigation()
          // The dispatcher can hold a reference to the host
          // so we need to null it out to prevent memory leaks.
          this.remove()
        }
      }
    )

    if (platformParameterStates.isNotEmpty()) {
      this.platformParameterStates = platformParameterStates.toMutableMap()
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)
    bindingAdapter = createRecyclerViewAdapter()
    binding.platformParametersRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = platformParametersViewModel
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

  fun onBackNavigation() {
    val overriddenPlatformParameters = platformParameterStates.map { (id, value) ->
      OverriddenPlatformParameter.newBuilder()
        .setId(id)
        .setOverriddenValue(value)
        .build()
    }

    platformParameterControllerDebugImpl
      .updateOverriddenPlatformParameters(overriddenPlatformParameters)
      .toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Success -> (activity as PlatformParametersActivity).finish()
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "PlatformParametersFragmentPresenter",
              "Failed to override platform parameters: ",
              it.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
    (activity as PlatformParametersActivity).finish()
  }

  private fun bindPlatformParameterItem(
    binding: PlatformParameterItemBinding,
    model: PlatformParameterItemViewModel
  ) {
    binding.viewModel = model
    val editText = binding.platformParameterInputEditText
    val previousWatcher = editText.getTag(R.id.platform_parameter_text_watcher) as? TextWatcher
    previousWatcher?.let { editText.removeTextChangedListener(it) }

    if (model.currentValue.hasBoolean()) {
      handleBooleanParameter(model)
    } else {
      handleTextInputParameter(model, editText)
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

  private fun handleTextInputParameter(
    model: PlatformParameterItemViewModel,
    editText: TextInputEditText
  ) {
    val paramState = platformParameterStates[model.platformParameterId]

    when {
      model.currentValue.hasInteger() -> {
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        val displayValue = when (val storedValue = paramState?.integer) {
          -1 -> {
            model.inputErrorMsg.set(invalidInputErrorText)
            ""
          }
          null -> {
            model.inputErrorMsg.set("")
            model.currentValue.integer.toString()
          }
          else -> {
            model.inputErrorMsg.set("")
            storedValue.toString()
          }
        }
        model.inputValue.set(displayValue)
      }

      model.currentValue.hasString() -> {
        editText.inputType = InputType.TYPE_CLASS_TEXT
        model.inputValue.set(paramState?.string ?: model.currentValue.string)
        model.inputErrorMsg.set("")
      }
    }

    editText.setTag(R.id.platform_parameter_text_change_flag, true)

    model.onPlatformParameterTextChangedCallback =
      onPlatformParameterTextChangedCallback@{ id, text ->
        val ignoreInitialBinding =
          editText.getTag(R.id.platform_parameter_text_change_flag) as? Boolean ?: false
        if (ignoreInitialBinding) {
          editText.setTag(R.id.platform_parameter_text_change_flag, false)
          return@onPlatformParameterTextChangedCallback
        }

        when {
          model.currentValue.hasInteger() -> {
            if (text == model.currentValue.integer.toString()) {
              platformParameterStates.remove(id)
            } else {
              val parsed = text.toIntOrNull()
              if (parsed == null) {
                model.inputErrorMsg.set(invalidInputErrorText)
                platformParameterStates[id] =
                  PlatformParameterValue.newBuilder().setInteger(-1).build()
              } else {
                model.inputErrorMsg.set("")
                platformParameterStates[id] =
                  PlatformParameterValue.newBuilder().setInteger(parsed).build()
              }
            }
          }
          model.currentValue.hasString() -> {
            if (text == model.currentValue.string) {
              platformParameterStates.remove(id)
            } else {
              if (text.isBlank()) {
                model.inputErrorMsg.set(invalidInputErrorText)
              } else {
                model.inputErrorMsg.set("")
              }
              platformParameterStates[id] = PlatformParameterValue.newBuilder()
                .setString(text)
                .build()
            }
          }
        }
      }
  }

  private fun handleBooleanParameter(model: PlatformParameterItemViewModel) {
    if (platformParameterStates.containsKey(model.platformParameterId)) {
      model.isChecked.set(platformParameterStates[model.platformParameterId]?.boolean)
    }

    model.onPlatformParameterToggledCallback = { id, value ->
      if (value == model.currentValue.boolean) {
        platformParameterStates.remove(id)
      } else {
        platformParameterStates[id] = PlatformParameterValue.newBuilder()
          .setBoolean(value)
          .build()
      }
    }
  }
}
