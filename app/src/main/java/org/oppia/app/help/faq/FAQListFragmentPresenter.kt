package org.oppia.app.help.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.FaqContentBinding
import org.oppia.app.databinding.FaqItemHeaderBinding
import org.oppia.app.databinding.FaqListFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [FAQListFragment]. */
@FragmentScope
class FAQListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<FAQListViewModel>
) {
  private lateinit var binding: FaqListFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val viewModel = getFAQListViewModel()

    binding = FaqListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot = */ false
    )

    binding.faqFragmentRecyclerView.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<FAQItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<FAQItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is FAQHeaderViewModel -> ViewType.VIEW_TYPE_HEADER
          is FAQContentViewModel -> ViewType.VIEW_TYPE_CONTENT
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER,
        inflateDataBinding = FaqItemHeaderBinding::inflate,
        setViewModel = FaqItemHeaderBinding::setViewModel,
        transformViewModel = { it as FAQHeaderViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CONTENT,
        inflateDataBinding = FaqContentBinding::inflate,
        setViewModel = FaqContentBinding::setViewModel,
        transformViewModel = { it as FAQContentViewModel }
      )
      .build()
  }

  private fun getFAQListViewModel(): FAQListViewModel {
    return viewModelProvider.getForFragment(fragment, FAQListViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CONTENT
  }
}
