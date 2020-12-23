package org.oppia.android.app.home

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Provides
import org.oppia.android.R
import org.oppia.android.app.home.promotedlist.PromotedStoryListViewModel
import org.oppia.android.app.home.topiclist.AllTopicsViewModel
import org.oppia.android.app.home.topiclist.TopicSummaryViewModel
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AllTopicsBinding
import org.oppia.android.databinding.PromotedStoryListBinding
import org.oppia.android.databinding.TopicSummaryViewBinding
import org.oppia.android.databinding.WelcomeBinding
import javax.inject.Inject

/** A custom [RecyclerView] for the HomeActivity that uses a GridLayoutManager. */
class HomeFragmentView @JvmOverloads constructor (
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
): RecyclerView(context, attrs, defStyleAttr) {

  @Inject
  private lateinit var activity: AppCompatActivity

  init {
    adapter = createAdapter()

    val spanCount = activity.resources.getInteger(R.integer.home_span_count)
    val homeLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    homeLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (this@HomeFragmentView.adapter?.getItemViewType(position) === ViewType.TOPIC_LIST.ordinal) {
          1
        } else spanCount
      }
    }
    layoutManager = homeLayoutManager
  }

  private fun createAdapter(): BindableAdapter<HomeItemViewModel>  {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<HomeItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is WelcomeViewModel -> ViewType.WELCOME_MESSAGE
          is PromotedStoryListViewModel -> ViewType.PROMOTED_STORY_LIST
          is AllTopicsViewModel -> ViewType.ALL_TOPICS
          is TopicSummaryViewModel -> ViewType.TOPIC_LIST
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.WELCOME_MESSAGE,
        inflateDataBinding = WelcomeBinding::inflate,
        setViewModel = WelcomeBinding::setViewModel,
        transformViewModel = { it as WelcomeViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.PROMOTED_STORY_LIST,
        inflateDataBinding = PromotedStoryListBinding::inflate,
        setViewModel = PromotedStoryListBinding::setViewModel,
        transformViewModel = { it as PromotedStoryListViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.ALL_TOPICS,
        inflateDataBinding = AllTopicsBinding::inflate,
        setViewModel = AllTopicsBinding::setViewModel,
        transformViewModel = { it as AllTopicsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.TOPIC_LIST,
        inflateDataBinding = TopicSummaryViewBinding::inflate,
        setViewModel = TopicSummaryViewBinding::setViewModel,
        transformViewModel = { it as TopicSummaryViewModel }
      )
      .build()
  }

  private enum class ViewType {
    WELCOME_MESSAGE,
    PROMOTED_STORY_LIST,
    ALL_TOPICS,
    TOPIC_LIST
  }
}