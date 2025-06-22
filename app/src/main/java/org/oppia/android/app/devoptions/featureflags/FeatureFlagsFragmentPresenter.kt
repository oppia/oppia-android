package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagsFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagsItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import javax.inject.Inject

/** The presenter for [FeatureFlagsFragment]. */
@FragmentScope
class FeatureFlagsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val featureFlagsViewModel: FeatureFlagsViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private lateinit var binding: FeatureFlagsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<FeatureFlagItemViewModel>

  /** List of feature flag switch states to be used in the fragment. */
  var featureFlagStates: ArrayList<Boolean> = arrayListOf()

  /** Called when [FeatureFlagsFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    featureFlagStates: ArrayList<Boolean>
  ): View? {
    binding = FeatureFlagsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.featureFlagsToolbar.setNavigationOnClickListener {
      onBackNavigation()
    }

    if (featureFlagStates.isNotEmpty()) {
      this.featureFlagStates = featureFlagStates
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
    (activity as FeatureFlagsActivity).finish()
  }

  private fun bindFeatureFlagItem(
    binding: FeatureFlagsItemBinding,
    model: FeatureFlagItemViewModel
  ) {
    binding.viewModel = model
//    val index = featureFlagsViewModel.featureFlagList.value?.indexOf(model)!!
//    if (featureFlagStates.size != featureFlagsViewModel.featureFlagList.value?.size)
//      featureFlagStates.add(model.currentValue)
//
//    binding.isEnabled = featureFlagStates[index]
//
//    binding.featureFlagSwitch.setOnCheckedChangeListener { _, isChecked ->
//      featureFlagStates[index] = isChecked
//    }
    }
}
