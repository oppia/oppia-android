package org.oppia.app.home.topiclist

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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

  private val orientation = Resources.getSystem().configuration.orientation
  private val metrics = DisplayMetrics()
  private var screenWidth = 0

  init {
    activity.windowManager.defaultDisplay.getMetrics(metrics)
    screenWidth = metrics.widthPixels
  }

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
      else -> {
        throw IllegalArgumentException(
          "Invalid type of data $position with item ${itemList[position]}"
        )
      }
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class WelcomeViewHolder(val binding: WelcomeBinding) :
    RecyclerView.ViewHolder(binding.root) {
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
      val promotedStoryAdapter = PromotedStoryListAdapter(activity, promotedStoryList)
      val horizontalLayoutManager =
        LinearLayoutManager(
          activity,
          LinearLayoutManager.HORIZONTAL,
          /* reverseLayout= */ false
        )
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
      binding.promotedStoryListRecyclerView.setOnFlingListener(null)
      snapHelper.attachToRecyclerView(binding.promotedStoryListRecyclerView)

      val paddingEnd = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        (activity as Context).resources.getDimensionPixelSize(R.dimen.padding_44)
      } else {
        (activity as Context).resources.getDimensionPixelSize(R.dimen.padding_72)
      }
      val paddingStart = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        (activity as Context).resources.getDimensionPixelSize(R.dimen.padding_20)
      } else {
        (activity as Context).resources.getDimensionPixelSize(R.dimen.padding_72)
      }
      if (promotedStoryList.size > 1) {
        binding.promotedStoryListRecyclerView
          .setPadding(
            paddingStart,
            0,
            paddingEnd,
            0
          )
      } else {
        binding.promotedStoryListRecyclerView
          .setPadding(
            paddingStart,
            0,
            paddingStart,
            0
          )
      }
    }
  }

  private class AllTopicsViewHolder(binding: AllTopicsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind() {
    }
  }

  inner class TopicListViewHolder(val binding: TopicSummaryViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(topicSummaryViewModel: TopicSummaryViewModel, position: Int) {
      binding.viewModel = topicSummaryViewModel

      val marginLayoutParams =
        binding.topicContainer.layoutParams as ViewGroup.MarginLayoutParams

      val marginMax = if (activity.resources.getBoolean(R.bool.isTablet)) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_120)
        } else {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_152)
        }
      } else {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_28)
        } else {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_72)
        }
      }

      val marginTopBottom = (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_12)

      val marginMin = if (activity.resources.getBoolean(R.bool.isTablet)) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          val singleItemWidth = (screenWidth / 3) - marginMax
          (screenWidth / 6) - (singleItemWidth / 2)
        } else {
          val singleItemWidth = (screenWidth / 4) - marginMax
          (screenWidth - 4 * singleItemWidth) / 6
        }
      } else {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_8)
        } else {
          (activity as Context).resources.getDimensionPixelSize(R.dimen.margin_36)
        }
      }

      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        val maxItemsInARow = if (activity.resources.getBoolean(R.bool.isTablet)) {
          3
        } else {
          2
        }
        if (activity.resources.getBoolean(R.bool.isTablet)) {
          when {
            position % maxItemsInARow == 0 -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              0,
              marginTopBottom
            )
            position % maxItemsInARow == 1 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
            else -> marginLayoutParams.setMargins(
              0,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
          }
        } else {
          when {
            position % maxItemsInARow == 0 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
            else -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
          }
        }
      } else {
        val maxItemsInARow = if (activity.resources.getBoolean(R.bool.isTablet)) {
          4
        } else {
          3
        }
        if (activity.resources.getBoolean(R.bool.isTablet)) {
          when {
            (position + 1) % maxItemsInARow == 0 -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              0,
              marginTopBottom
            )
            (position + 1) % maxItemsInARow == 1 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMin / 2,
              marginTopBottom
            )
            (position + 1) % maxItemsInARow == 2 -> marginLayoutParams.setMargins(
              marginMin / 2,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
            (position + 1) % maxItemsInARow == 3 -> marginLayoutParams.setMargins(
              0,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
          }
        } else {
          when {
            position % maxItemsInARow == 0 -> marginLayoutParams.setMargins(
              marginMax,
              marginTopBottom,
              /* right= */ 0,
              marginTopBottom
            )
            position % maxItemsInARow == 1 -> marginLayoutParams.setMargins(
              marginMin,
              marginTopBottom,
              marginMin,
              marginTopBottom
            )
            position % maxItemsInARow == 2 -> marginLayoutParams.setMargins(
              /* left= */ 0,
              marginTopBottom,
              marginMax,
              marginTopBottom
            )
          }
        }
      }
      binding.topicContainer.layoutParams = marginLayoutParams
    }
  }
}
