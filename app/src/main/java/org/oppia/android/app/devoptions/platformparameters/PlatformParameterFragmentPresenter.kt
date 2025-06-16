package org.oppia.android.app.devoptions.platformparameters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.PlatformParameterFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject
import org.oppia.android.app.databinding.databinding.PlatformParameterItemBinding
import org.oppia.android.app.recyclerview.BindableAdapter

/** The presenter for [PlatformParameterFragment]. */
@FragmentScope
class PlatformParameterFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val platformParameterViewModel: PlatformParameterViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private lateinit var binding: PlatformParameterFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<PlatformParameterItemViewModel>

  /** Called when [PlatformParameterFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = PlatformParameterFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.platformParameterToolbar.setNavigationOnClickListener {
      onBackNavigation()
    }
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)
    bindingAdapter = createRecyclerViewAdapter()
    binding.platformParameterRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }
    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = platformParameterViewModel
    }

    return binding.root
  }
  
  private fun onBackNavigation() {
    (activity as PlatformParameterActivity).finish()
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
    if(model.currentValue.hasBoolean())
    {
      binding.isEnabled = model.currentValue.boolean
      binding.isInputVisible = false
    }
    else
    {
      binding.isInputVisible = true
      binding.inputValue = model.currentValue.string
    }
    binding.syncStatusValueTextView.setBackgroundResource(
      platformParameterViewModel.getSyncStatusBackground(model.syncStatus)
    )
  }
}
