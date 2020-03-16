package org.oppia.app.help.faq

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.FaqContentBinding
import org.oppia.app.databinding.FaqFragmentBinding
import org.oppia.app.databinding.FaqItemHeaderBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.faq.faqItemViewModel.FAQContentViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQHeaderViewModel
import org.oppia.app.help.faq.faqItemViewModel.FAQItemViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [FAQFragment]. */
@FragmentScope
class FAQFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<FAQViewModel>
) {
  private lateinit var binding: FaqFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val viewModel = getFAQViewModel()

    binding = FaqFragmentBinding.inflate(inflater, container, /* attachToRoot = */ false)

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

  private fun getFAQViewModel(): FAQViewModel {
    return viewModelProvider.getForFragment(fragment, FAQViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CONTENT
  }
}
