package org.oppia.app.home.topiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.AllTopicsBinding
import org.oppia.app.databinding.PromotedStoryCardBinding
import org.oppia.app.databinding.TopicSummaryViewBinding
import org.oppia.app.databinding.WelcomeBinding
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.UserAppHistoryViewModel

private const val VIEW_TYPE_WELCOME_MESSAGE = 1
private const val VIEW_TYPE_PROMOTED_STORY = 2
private const val VIEW_TYPE_ALL_TOPICS = 3
private const val VIEW_TYPE_TOPIC_LIST = 4

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class TopicListAdapter(
  private val itemList: MutableList<HomeItemViewModel>
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
      VIEW_TYPE_PROMOTED_STORY -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          PromotedStoryCardBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        PromotedStoryViewHolder(binding)
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
      VIEW_TYPE_PROMOTED_STORY -> {
        (holder as PromotedStoryViewHolder).bind(itemList[position] as PromotedStoryViewModel)
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
      is PromotedStoryViewModel -> {
        VIEW_TYPE_PROMOTED_STORY
      }
      is AllTopicsViewModel -> {
        VIEW_TYPE_ALL_TOPICS
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

  private class PromotedStoryViewHolder(
    val binding: PromotedStoryCardBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(promotedStoryViewModel: PromotedStoryViewModel) {
      binding.viewModel = promotedStoryViewModel
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
