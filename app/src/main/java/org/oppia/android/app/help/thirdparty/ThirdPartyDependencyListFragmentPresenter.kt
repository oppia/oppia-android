package org.oppia.android.app.help.thirdparty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ThirdPartyDependencyItemBinding
import org.oppia.android.databinding.ThirdPartyDependencyListFragmentBinding
import javax.inject.Inject

/** The presenter for [ThirdPartyDependencyListFragment]. */
@FragmentScope
class ThirdPartyDependencyListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ThirdPartyDependencyListViewModel>
) {
  private lateinit var binding: ThirdPartyDependencyListFragmentBinding

  /** Handles onCreateView() method of the [ThirdPartyDependencyListFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean
  ): View {
    val viewModel = getThirdPartyDependencyListViewModel()
    viewModel.isMultipane.set(isMultipane)
    binding = ThirdPartyDependencyListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    val recyclerviewAdapter = createRecyclerViewAdapter()

    binding.thirdPartyDependencyListFragmentRecyclerView.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
      adapter = recyclerviewAdapter
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ThirdPartyDependencyItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .Factory(fragment).create<ThirdPartyDependencyItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ThirdPartyDependencyItemBinding::inflate,
        setViewModel = ThirdPartyDependencyItemBinding::setViewModel
      )
      .build()
  }

  private fun getThirdPartyDependencyListViewModel(): ThirdPartyDependencyListViewModel {
    return viewModelProvider.getForFragment(fragment, ThirdPartyDependencyListViewModel::class.java)
  }
}
