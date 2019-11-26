package org.oppia.app.home.topiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.AllTopicsBinding
import org.oppia.app.databinding.PromotedStoryListBinding
import org.oppia.app.databinding.TopicSummaryViewBinding
import org.oppia.app.databinding.WelcomeBinding
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.UserAppHistoryViewModel
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper



private const val VIEW_TYPE_WELCOME_MESSAGE = 1
private const val VIEW_TYPE_PROMOTED_STORY_LIST = 2
private const val VIEW_TYPE_ALL_TOPICS = 3
private const val VIEW_TYPE_TOPIC_LIST = 4

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class TopicListAdapter(
  private val activity: AppCompatActivity,
  private val itemList: MutableList<HomeItemViewModel>,
  private val promotedStoryList: MutableList<PromotedStoryViewModel>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_WELCOME_MESSAGE -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          WelcomeBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        WelcomeViewHolder(binding)
      }
      VIEW_TYPE_PROMOTED_STORY_LIST -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          PromotedStoryListBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        PromotedStoryListViewHolder(binding)
      }
      VIEW_TYPE_ALL_TOPICS -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          AllTopicsBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        AllTopicsViewHolder(binding)
      }
      VIEW_TYPE_TOPIC_LIST -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicSummaryViewBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        TopicListViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_WELCOME_MESSAGE -> {
        (holder as WelcomeViewHolder).bind(itemList[position] as UserAppHistoryViewModel)
      }
      VIEW_TYPE_PROMOTED_STORY_LIST -> {
        (holder as PromotedStoryListViewHolder).bind(activity, promotedStoryList)
      }
      VIEW_TYPE_ALL_TOPICS -> {
        (holder as AllTopicsViewHolder).bind()
      }
      VIEW_TYPE_TOPIC_LIST -> {
        (holder as TopicListViewHolder).bind(itemList[position] as TopicSummaryViewModel)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is UserAppHistoryViewModel -> {
        VIEW_TYPE_WELCOME_MESSAGE
      }
      is AllTopicsViewModel -> {
        VIEW_TYPE_ALL_TOPICS
      }
      is PromotedStoryListViewModel -> {
        VIEW_TYPE_PROMOTED_STORY_LIST
      }
      is TopicSummaryViewModel -> {
        VIEW_TYPE_TOPIC_LIST
      }
      else -> throw IllegalArgumentException("Invalid type of data $position with item ${itemList[position]}")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class WelcomeViewHolder(
    val binding: WelcomeBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(userAppHistoryViewModel: UserAppHistoryViewModel) {
      binding.viewModel = userAppHistoryViewModel
    }
  }

  private class PromotedStoryListViewHolder(
    val binding: PromotedStoryListBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(activity: AppCompatActivity, promotedStoryList: MutableList<PromotedStoryViewModel>) {
      val promotedStoryAdapter = PromotedStoryListAdapter(promotedStoryList)
      val horizontalLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
      binding.promotedStoryListRecyclerView.apply {
        layoutManager = horizontalLayoutManager
        adapter = promotedStoryAdapter
      }

      val snapHelper = PagerSnapHelper()
      binding.promotedStoryListRecyclerView.layoutManager = horizontalLayoutManager
      snapHelper.attachToRecyclerView(binding.promotedStoryListRecyclerView)
    }
  }

  private class AllTopicsViewHolder(
    val binding: AllTopicsBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind() {
    }
  }

  private class TopicListViewHolder(
    val binding: TopicSummaryViewBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(topicSummaryViewModel: TopicSummaryViewModel) {
      binding.viewModel = topicSummaryViewModel
    }
  }
}
