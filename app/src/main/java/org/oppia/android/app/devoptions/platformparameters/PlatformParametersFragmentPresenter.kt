package org.oppia.android.app.devoptions.platformparameters

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.PlatformParameterItemBinding
import org.oppia.android.app.databinding.databinding.PlatformParametersFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import javax.inject.Inject

/** The presenter for [PlatformParametersFragment]. */
@FragmentScope
class PlatformParametersFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val platformParametersViewModel: PlatformParametersViewModel,
  resourceHandler: AppLanguageResourceHandler,
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

  private fun onBackNavigation() {
    (activity as PlatformParametersActivity).finish()
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<PlatformParameterItemViewModel> {
    return singleTypeBuilderFactory.create<PlatformParameterItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = PlatformParameterItemBinding::inflate,
        setViewModel = this::bindPlatformParameterItem
      )
      .build()
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
      if (platformParameterStates.containsKey(model.platformParameterId)) {
        model.isChecked.set(platformParameterStates[model.platformParameterId]?.boolean)
      }
      model.onPlatformParameterToggleCallback = { id, value ->
        platformParameterStates[id] = PlatformParameterValue.newBuilder()
          .setBoolean(value)
          .build()
      }
    } else {
      val paramState = platformParameterStates[model.platformParameterId]

      when {
        model.currentValue.hasInteger() -> {
          editText.inputType = InputType.TYPE_CLASS_NUMBER
          val displayValue = when (val storedValue = paramState?.integer) {
            -1 -> {
              model.errorMessage.set(invalidInputErrorText)
              ""
            }
            null -> model.currentValue.integer.toString()
            else -> storedValue.toString()
          }
          model.inputValue.set(displayValue)
        }
        model.currentValue.hasString() -> {
          editText.inputType = InputType.TYPE_CLASS_TEXT
          if (paramState != null) {
            model.inputValue.set(paramState.string)
          }
        }
        else -> {
          editText.inputType = InputType.TYPE_CLASS_TEXT
        }
      }
      editText.setText(model.inputValue.get() ?: "")
      if (!model.inputValue.get().isNullOrEmpty()) {
        model.errorMessage.set("")
      }
      model.onPlatformParameterTextChangedCallback = { id, text ->
        when {
          model.currentValue.hasInteger() -> {

            val parsed = text.toIntOrNull()
            if (parsed == null || text.isBlank()) {
              model.errorMessage.set(invalidInputErrorText)
              platformParameterStates[id] =
                PlatformParameterValue.newBuilder().setInteger(-1).build()
            } else {
              model.errorMessage.set("")
              platformParameterStates[id] =
                PlatformParameterValue.newBuilder().setInteger(parsed).build()
            }
          }
          model.currentValue.hasString() -> {
            if (text.isNullOrEmpty()) {
              model.errorMessage.set(invalidInputErrorText)
              platformParameterStates[id] = PlatformParameterValue.newBuilder()
                .setString("")
                .build()
            } else {
              model.errorMessage.set("")
              platformParameterStates[id] = PlatformParameterValue.newBuilder()
                .setString(text)
                .build()
            }
          }
        }
      }
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
}
