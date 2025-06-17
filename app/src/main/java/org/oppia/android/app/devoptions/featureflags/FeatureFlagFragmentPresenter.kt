package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import javax.inject.Inject
import org.oppia.android.domain.oppialogger.OppiaLogger

/** The presenter for [FeatureFlagFragment]. */
@FragmentScope
class FeatureFlagFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val featureFlagViewModel: FeatureFlagViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private lateinit var binding: FeatureFlagFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<FeatureFlagItemViewModel>
  var featureFlagStates: ArrayList<Boolean> = arrayListOf()

  /** Called when [FeatureFlagFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    featureFlagStates: ArrayList<Boolean>
  ): View? {
    binding = FeatureFlagFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.featureFlagToolbar.setNavigationOnClickListener {
      onBackNavigation()
    }

    if (featureFlagStates.isNotEmpty()) {
      this.featureFlagStates = featureFlagStates
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = featureFlagViewModel
    }
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.featureFlagRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<FeatureFlagItemViewModel> {
    return singleTypeBuilderFactory.create<FeatureFlagItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = FeatureFlagItemBinding::inflate,
        setViewModel = this::bindFeatureFlagItem
      )
      .build()
  }

  private fun onBackNavigation() {
    (activity as FeatureFlagActivity).finish()
  }

  private fun bindFeatureFlagItem(
    binding: FeatureFlagItemBinding,
    model: FeatureFlagItemViewModel
  ) {
    binding.viewModel = model
    val index = featureFlagViewModel.featureFlagList.value?.indexOf(model)!!
    if (featureFlagStates.size != featureFlagViewModel.featureFlagList.value?.size)
      featureFlagStates.add(model.currentValue)

    binding.isEnabled = featureFlagStates[index]

    binding.featureFlagSwitch.setOnCheckedChangeListener { _, isChecked ->
      featureFlagStates[index] = isChecked
    }
    binding.syncStatusValueTextView.setBackgroundResource(
      featureFlagViewModel.getSyncStatusBackground(model.syncStatus)
    )
  }
}
