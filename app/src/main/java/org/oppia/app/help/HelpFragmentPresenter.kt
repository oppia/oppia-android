package org.oppia.app.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.HelpFragmentBinding
import org.oppia.app.databinding.HelpItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class HelpFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HelpListViewModel>
) {
  private lateinit var binding: HelpFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val viewModel = getHelpListViewModel()

    binding = HelpFragmentBinding.inflate(inflater, container, /* attachToRoot = */ false)
    binding.helpFragmentRecyclerView.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HelpItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<HelpItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = HelpItemBinding::inflate,
        setViewModel = HelpItemBinding::setViewModel
      ).build()
  }

  private fun getHelpListViewModel(): HelpListViewModel {
    return viewModelProvider.getForFragment(fragment, HelpListViewModel::class.java)
  }
}
