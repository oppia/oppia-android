package org.oppia.app.home.topiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.library.baseAdapters.BR
import org.oppia.app.R
import org.oppia.app.databinding.PromotedStoryCardBinding
import org.oppia.app.databinding.TopicSummaryViewBinding
import org.oppia.app.model.TopicSummary

private const val VIEW_TYPE_PROMOTED_STORY = 1
private const val VIEW_TYPE_TOPIC_LIST = 2

/** Adapter to inflate different items/views inside [RecyclerView]. The itemList consists of various ViewModels. */
class TopicListAdapter(
  private val itemList: MutableList<Any>
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  lateinit var promotedStoryViewModel: PromotedStoryViewModel

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#249): Generalize this binding to make adding future items easier.
      VIEW_TYPE_PROMOTED_STORY -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<PromotedStoryCardBinding>(
            inflater,
            R.layout.promoted_story_card,
            parent,
            /* attachToParent= */false
          )
        PromotedStoryViewHolder(binding)
      }
      VIEW_TYPE_TOPIC_LIST -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          DataBindingUtil.inflate<TopicSummaryViewBinding>(
            inflater,
            R.layout.topic_summary_view,
            parent,
            /* attachToParent= */false
          )
        TopicListViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type") as Throwable
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_PROMOTED_STORY -> {
        (holder as PromotedStoryViewHolder).bind(itemList[position] as PromotedStoryViewModel)
      }
      VIEW_TYPE_TOPIC_LIST -> {
        (holder as TopicListViewHolder).bind(itemList[position] as TopicSummary)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is PromotedStoryViewModel -> {
        promotedStoryViewModel = itemList[position] as PromotedStoryViewModel
        VIEW_TYPE_PROMOTED_STORY
      }

      is TopicSummary -> {
        itemList[position] as TopicSummary
        VIEW_TYPE_TOPIC_LIST
      }
      else -> throw IllegalArgumentException("Invalid type of data $position")
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class PromotedStoryViewHolder(
    val binding: ViewDataBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(promotedStoryViewModel: PromotedStoryViewModel) {
      binding.setVariable(BR.viewModel, promotedStoryViewModel)
    }
  }

  private class TopicListViewHolder(
    val binding: ViewDataBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(topicSummary: TopicSummary) {

      val topicSummaryViewModel = TopicSummaryViewModel(topicSummary)

      binding.setVariable(BR.viewModel, topicSummaryViewModel)
    }
  }



}
