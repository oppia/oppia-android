package org.oppia.android.app.walkthrough.topiclist

import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.walkthrough.WalkthroughActivity
import org.oppia.android.app.walkthrough.WalkthroughFragmentChangeListener
import org.oppia.android.app.walkthrough.WalkthroughPages
import org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicHeaderViewModel
import org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel.WalkthroughTopicSummaryViewModel
import org.oppia.android.databinding.WalkthroughTopicHeaderViewBinding
import org.oppia.android.databinding.WalkthroughTopicListFragmentBinding
import org.oppia.android.databinding.WalkthroughTopicSummaryViewBinding
import javax.inject.Inject

/** The presenter for [WalkthroughTopicListFragment]. */
@FragmentScope
class WalkthroughTopicListFragmentPresenter @Inject constructor(
  val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: WalkthroughTopicViewModel
) {
  private lateinit var binding: WalkthroughTopicListFragmentBinding
  private val routeToNextPage = activity as WalkthroughFragmentChangeListener
  private val orientation = Resources.getSystem().configuration.orientation

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val profileId = ProfileId.newBuilder().apply {
      internalId = activity.intent.getIntExtra(
        WalkthroughActivity.WALKTHROUGH_ACTIVITY_INTERNAL_PROFILE_ID_KEY, /* defaultValue= */ -1
      )
    }.build()
    viewModel.initialize(profileId)

    binding =
      WalkthroughTopicListFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )

    binding.let {
      it.lifecycleOwner = fragment
    }
    val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      2
    } else {
      3
    }
    val walkthroughLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    walkthroughLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0) {
          /* number of spaces this item should occupy = */ spanCount
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

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_TOPIC
  }

  fun changePage(topicSummary: TopicSummary) {
    routeToNextPage.pageWithTopicId(WalkthroughPages.FINAL.value, topicSummary.topicId)
  }
}
