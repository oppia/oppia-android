package org.oppia.app.home.topiclist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.AllTopicsBinding
import org.oppia.app.databinding.PromotedStoryListBinding
import org.oppia.app.databinding.TopicSummaryViewBinding
import org.oppia.app.databinding.WelcomeBinding
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.WelcomeViewModel
import org.oppia.app.recyclerview.StartSnapHelper

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
        (holder as WelcomeViewHolder).bind(itemList[position] as WelcomeViewModel)
      }
      VIEW_TYPE_PROMOTED_STORY_LIST -> {
        (holder as PromotedStoryListViewHolder).bind(
          activity,
          itemList[position] as PromotedStoryListViewModel,
          promotedStoryList
        )
      }
      VIEW_TYPE_ALL_TOPICS -> {
        (holder as AllTopicsViewHolder).bind()
      }
      VIEW_TYPE_TOPIC_LIST -> {
        (holder as TopicListViewHolder).bind(itemList[position] as TopicSummaryViewModel, position)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is WelcomeViewModel -> {
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

  private class WelcomeViewHolder(val binding: WelcomeBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(welcomeViewModel: WelcomeViewModel) {
      binding.viewModel = welcomeViewModel
    }
  }

  inner class PromotedStoryListViewHolder(val binding: PromotedStoryListBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(
      activity: AppCompatActivity,
      promotedStoryListViewModel: PromotedStoryListViewModel,
      promotedStoryList: MutableList<PromotedStoryViewModel>
    ) {
      binding.viewModel = promotedStoryListViewModel
      val promotedStoryAdapter = PromotedStoryListAdapter(promotedStoryList)
      val horizontalLayoutManager =
        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, /* reverseLayout= */ false)
      binding.promotedStoryListRecyclerView.apply {
        layoutManager = horizontalLayoutManager
        adapter = promotedStoryAdapter
      }

      /*
       * The StartSnapHelper is used to snap between items rather than smooth scrolling,
       * so that the item is completely visible in [HomeFragment] as soon as learner lifts the finger after scrolling.
       */
      val snapHelper = StartSnapHelper()
      binding.promotedStoryListRecyclerView.layoutManager = horizontalLayoutManager
      snapHelper.attachToRecyclerView(binding.promotedStoryListRecyclerView)

      val padding48 = (activity as Context).resources.getDimensionPixelSize(R.dimen.padding_48)
      val padding20 = (activity as Context).resources.getDimensionPixelSize(R.dimen.padding_20)
      if (promotedStoryList.size > 1) {
        binding.promotedStoryListRecyclerView.setPadding(padding20, 0, padding48, 0)
      } else {
        binding.promotedStoryListRecyclerView.setPadding(padding20, 0, padding20, 0)
      }
    }
  }

  private class AllTopicsViewHolder(binding: AllTopicsBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind() {
    }
  }

  inner class TopicListViewHolder(val binding: TopicSummaryViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(topicSummaryViewModel: TopicSummaryViewModel, position: Int) {
      binding.viewModel = topicSummaryViewModel
      val param = binding.topicContainer.layoutParams as GridLayoutManager.LayoutParams
      val margin32 = (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_32)
      val margin8 = (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_8)
      if (position % 2 == 0) {
        param.setMargins(margin8, margin8, margin32, margin8)
      } else {
        param.setMargins(margin32, margin8, margin8, margin8)
      }
      binding.topicContainer.layoutParams = param
    }
  }
}
