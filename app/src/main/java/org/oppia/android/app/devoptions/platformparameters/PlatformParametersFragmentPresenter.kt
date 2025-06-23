package org.oppia.android.app.devoptions.platformparameters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.PlatformParameterItemBinding
import org.oppia.android.app.databinding.databinding.PlatformParametersFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject
import org.oppia.android.app.model.PlatformParameterDefinition

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
  var platformParameterStates: MutableList<PlatformParameterDefinition> = mutableListOf()

  /** Called when [PlatformParametersFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    platformParameterStates: MutableList<PlatformParameterDefinition>
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
      this.platformParameterStates = platformParameterStates
    }
    oppiaLogger.d("PlatformParametersFragment", platformParameterStates.toString())
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
    if()



//    val index = platformParametersViewModel.platformParameterList.value?.indexOf(model)!!
//    val totalSize = platformParametersViewModel.platformParameterList.value?.size
//
//    if (platformParameterStates.size != totalSize)
//      platformParameterStates.add(model.currentValue)
//
//    oppiaLogger.d(
//      "PlatformParametersFragment",
//      "Index is: $index, " +
//        "Total Size is: $totalSize, Current Value is: ${platformParameterStates[index].integer}" +
//        "${platformParameterStates[index].string} ${platformParameterStates[index].boolean}"
//    )
//    if (model.currentValue.hasBoolean()) {
//
//      binding.isEnabled = platformParameterStates[index].boolean
//      binding.platformParameterSwitch.setOnCheckedChangeListener { _, isChecked ->
//        platformParameterStates[index] = PlatformParameterValue.newBuilder()
//          .setBoolean(isChecked)
//          .build()
//      }
//      binding.isInputVisible = false
//    } else if (model.currentValue.hasInteger()) {
//      binding.inputValue = platformParameterStates[index].integer.toString()
//      binding.isInputVisible = true
//      binding.platformParameterInputEditText.inputType = InputType.TYPE_CLASS_NUMBER
//
//      binding.platformParameterInputEditText.onTextChanged { inputValue ->
//        if (!inputValue.isNullOrEmpty()) {
//          platformParameterStates[index] = PlatformParameterValue.newBuilder()
//            .setInteger(inputValue.toInt())
//            .build()
//        } else {
//          binding.platformParameterInputLayout.error = "Invalid Input"
//        }
//      }
//    } else {
//      binding.isInputVisible = true
//      binding.inputValue = platformParameterStates[index].string
//    }
//    binding.syncStatusValueTextView.setBackgroundResource(
//      platformParametersViewModel.getSyncStatusBackground(model.syncStatus)
//    )
  }
}
