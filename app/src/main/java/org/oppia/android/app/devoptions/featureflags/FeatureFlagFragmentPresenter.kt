package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import javax.inject.Inject

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

  /** Called when [FeatureFlagFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = FeatureFlagFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.featureFlagToolbar.setNavigationOnClickListener {
      onBackNavigation()
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
    binding.isEnabled = model.currentValue
    binding.syncStatusValueTextView.setBackgroundResource(
      featureFlagViewModel.getSyncStatusBackground(model.syncStatus)
    )
  }
}
