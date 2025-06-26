package org.oppia.android.app.devoptions.platformparameters

import android.text.InputType
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
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject

/** The presenter for [PlatformParametersFragment]. */
@FragmentScope
class PlatformParametersFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val platformParametersViewModel: PlatformParametersViewModel,
  private val oppiaLogger: OppiaLogger,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private lateinit var binding: PlatformParametersFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<PlatformParameterItemViewModel>
  var platformParameterStates:
    MutableMap<PlatformParameterId, PlatformParameterValue> = mutableMapOf()

  /** Called when [PlatformParametersFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    platformParameterStates: Map<PlatformParameterId, PlatformParameterValue>
  ): View? {
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
    oppiaLogger.d("PlatformParametersFragment", "States are: $platformParameterStates")
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
    // binding.platformParameterInputLayout.error ="Invalid Input"
    if (model.currentValue.hasBoolean()) {

      if (platformParameterStates.containsKey(model.platformParameterId)) {
        model.isChecked.set(platformParameterStates[model.platformParameterId]?.boolean)
      }
      model.onToggleCallback = { id, value ->
        platformParameterStates[id] = PlatformParameterValue.newBuilder()
          .setBoolean(value)
          .build()
      }
    } else if (model.currentValue.hasInteger()) {
      binding.platformParameterInputEditText.inputType = InputType.TYPE_CLASS_NUMBER
      if (platformParameterStates.containsKey(model.platformParameterId)) {
        if (platformParameterStates[model.platformParameterId]?.integer == -1) {
          binding.platformParameterInputLayout.error = "Invalid Input"
          model.inputValue.set("")
        } else {
          binding.platformParameterInputLayout.error = null
          model.inputValue
            .set(platformParameterStates[model.platformParameterId]?.integer.toString())
        }
      }
      binding.platformParameterInputEditText.onTextChanged { value ->
        if (value.isNullOrEmpty() || value.toIntOrNull() == null) {
          binding.platformParameterInputLayout.error = "Invalid Input"
          platformParameterStates[model.platformParameterId] = PlatformParameterValue.newBuilder()
            .setInteger(-1)
            .build()
        } else {
          binding.platformParameterInputLayout.error = null
          platformParameterStates[model.platformParameterId] = PlatformParameterValue.newBuilder()
            .setInteger(value.toIntOrNull() ?: 0)
            .build()
        }
      }
    } else {
      binding.platformParameterInputEditText.inputType = InputType.TYPE_CLASS_TEXT
      if (platformParameterStates.containsKey(model.platformParameterId)) {
        model.inputValue.set(platformParameterStates[model.platformParameterId]?.string)
      }
      binding.platformParameterInputEditText.onTextChanged { value ->
        if (value.isNullOrEmpty()) {
          binding.platformParameterInputLayout.error = "Invalid Input"
          platformParameterStates[model.platformParameterId] = PlatformParameterValue.newBuilder()
            .setString("")
            .build()
        } else {
          binding.platformParameterInputLayout.error = null
          platformParameterStates[model.platformParameterId] = PlatformParameterValue.newBuilder()
            .setString(value)
            .build()
        }
      }
    }
  }
}
