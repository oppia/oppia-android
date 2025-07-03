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
      model.onFeatureFlagToggleCallback = { id, value ->
        platformParameterStates[id] = PlatformParameterValue.newBuilder()
          .setBoolean(value)
          .build()
      }
    } else {

      if (model.currentValue.hasInteger()) {
        binding.platformParameterInputEditText.inputType = InputType.TYPE_CLASS_NUMBER

        if (platformParameterStates.containsKey(model.platformParameterId)) {
          if (platformParameterStates[model.platformParameterId]?.integer == -1) {
            binding.platformParameterInputLayout.error = invalidInputErrorText
            model.inputValue.set("")
          } else {
            binding.platformParameterInputLayout.error = null
            model.inputValue
              .set(platformParameterStates[model.platformParameterId]?.integer.toString())
          }
        } else {
          model.inputValue.set(model.currentValue.integer.toString())
        }
        editText.setText(model.inputValue.get() ?: "")
        if (model.inputValue.get().toString().isNotEmpty()) {
          binding.platformParameterInputLayout.error = null
        }
        model.onTextChangedCallback = { id, text ->
          val parsed = text.toIntOrNull()
          if (parsed == null) {
            binding.platformParameterInputLayout.error = invalidInputErrorText
            platformParameterStates[id] =
              PlatformParameterValue.newBuilder().setInteger(-1).build()
          } else {
            binding.platformParameterInputLayout.error = null
            platformParameterStates[id] =
              PlatformParameterValue.newBuilder().setInteger(parsed).build()
          }
        }
      } else {

        binding.platformParameterInputEditText.inputType = InputType.TYPE_CLASS_TEXT
        if (platformParameterStates.containsKey(model.platformParameterId)) {
          model.inputValue.set(platformParameterStates[model.platformParameterId]?.string)
        }
        editText.setText(model.inputValue.get())
        model.onTextChangedCallback = { id, text ->
          if (text.isNullOrEmpty()) {
            binding.platformParameterInputLayout.error = invalidInputErrorText
            platformParameterStates[id] = PlatformParameterValue.newBuilder()
              .setString("")
              .build()
          } else {
            binding.platformParameterInputLayout.error = null
            platformParameterStates[id] = PlatformParameterValue.newBuilder()
              .setString(text)
              .build()
          }
        }
      }
    }

    val newWatcher = object : TextWatcher {
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        model.onTextChangedCallback?.invoke(model.platformParameterId, s.toString())
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun afterTextChanged(s: Editable?) {}
    }

    editText.addTextChangedListener(newWatcher)
    editText.setTag(R.id.platform_parameter_text_watcher, newWatcher)
  }
}
