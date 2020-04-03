package org.oppia.app.walkthrough.topiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.databinding.WalkthroughTopicHeaderViewBinding
import org.oppia.app.databinding.WalkthroughTopicListFragmentBinding
import org.oppia.app.databinding.WalkthroughTopicSummaryViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.TopicSummary
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.app.walkthrough.WalkthroughFragmentChangeListener
import org.oppia.app.walkthrough.WalkthroughPages
import org.oppia.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicHeaderViewModel
import org.oppia.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicSummaryViewModel
import javax.inject.Inject

/** The presenter for [WalkthroughTopicListFragment]. */
@FragmentScope
class WalkthroughTopicListFragmentPresenter @Inject constructor(
  val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<WalkthroughTopicViewModel>
) {
  private lateinit var binding: WalkthroughTopicListFragmentBinding
  private val routeToNextPage = activity as WalkthroughFragmentChangeListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val viewModel = getWalkthroughTopicViewModel()

    binding = WalkthroughTopicListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }
    val walkthroughLayoutManager = GridLayoutManager(activity.applicationContext, 2)
    walkthroughLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0) {
          /* number of spaces this item should occupy = */ 2
        } else {
          /* number of spaces this item should occupy = */ 1
        }
      }
    }
    binding.walkthroughTopicRecyclerView.apply {
      layoutManager = walkthroughLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<WalkthroughTopicItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<WalkthroughTopicItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is WalkthroughTopicHeaderViewModel -> ViewType.VIEW_TYPE_HEADER
          is WalkthroughTopicSummaryViewModel -> ViewType.VIEW_TYPE_TOPIC
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER,
        inflateDataBinding = WalkthroughTopicHeaderViewBinding::inflate,
        setViewModel = WalkthroughTopicHeaderViewBinding::setViewModel,
        transformViewModel = { it as WalkthroughTopicHeaderViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TOPIC,
        inflateDataBinding = WalkthroughTopicSummaryViewBinding::inflate,
        setViewModel = WalkthroughTopicSummaryViewBinding::setViewModel,
        transformViewModel = { it as WalkthroughTopicSummaryViewModel }
      )
      .build()
  }

  private fun getWalkthroughTopicViewModel(): WalkthroughTopicViewModel {
    return viewModelProvider.getForFragment(fragment, WalkthroughTopicViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_TOPIC
  }

  fun changePage(topicSummary: TopicSummary) {
    routeToNextPage.pageWithTopicId(WalkthroughPages.FINAL.value, topicSummary.topicId)
  }
}
