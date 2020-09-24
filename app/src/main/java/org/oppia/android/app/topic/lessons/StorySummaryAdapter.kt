package org.oppia.app.topic.lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.TopicLessonsStorySummaryBinding
import org.oppia.app.databinding.TopicLessonsTitleBinding
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

private const val VIEW_TYPE_TITLE_TEXT = 1
private const val VIEW_TYPE_STORY_ITEM = 2

/** Adapter to bind StorySummary to [RecyclerView] inside [TopicLessonsFragment]. */
class StorySummaryAdapter(
  private val itemList: MutableList<TopicLessonsItemViewModel>,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val expandedChapterListIndexListener: ExpandedChapterListIndexListener,
  private var currentExpandedChapterListIndex: Int?
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_TITLE_TEXT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicLessonsTitleBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        TopicPlayTitleViewHolder(binding)
      }
      VIEW_TYPE_STORY_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicLessonsStorySummaryBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        StorySummaryViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_TITLE_TEXT -> {
        (holder as TopicPlayTitleViewHolder).bind()
      }
      VIEW_TYPE_STORY_ITEM -> {
        (holder as StorySummaryViewHolder).bind(itemList[i] as StorySummaryViewModel, i)
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is TopicLessonsTitleViewModel -> {
        VIEW_TYPE_TITLE_TEXT
      }
      is StorySummaryViewModel -> {
        VIEW_TYPE_STORY_ITEM
      }
      else -> throw IllegalArgumentException(
        "Invalid type of data $position with item ${itemList[position]}"
      )
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class TopicPlayTitleViewHolder(
    binding: TopicLessonsTitleBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind() {}
  }

  inner class StorySummaryViewHolder(private val binding: TopicLessonsStorySummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(storySummaryViewModel: StorySummaryViewModel, position: Int) {
      var isChapterListVisible = false
      if (currentExpandedChapterListIndex != null) {
        isChapterListVisible = currentExpandedChapterListIndex!! == position
      }
      binding.isListExpanded = isChapterListVisible
      binding.viewModel = storySummaryViewModel

      val chapterSummaries = storySummaryViewModel
        .storySummary.chapterList
      val completedChapterCount =
        chapterSummaries.map(ChapterSummary::getChapterPlayState)
          .filter {
            it == ChapterPlayState.COMPLETED
          }
          .size
      val storyPercentage: Int =
        (completedChapterCount * 100) / storySummaryViewModel.storySummary.chapterCount
      binding.storyPercentage = storyPercentage
      binding.storyProgressView.setStoryChapterDetails(
        storySummaryViewModel.storySummary.chapterCount,
        completedChapterCount
      )
      binding.topicPlayStoryDashedLineView.setLayerType(
        View.LAYER_TYPE_SOFTWARE,
        /* paint= */ null
      )
      val chapterList = storySummaryViewModel.storySummary.chapterList
      binding.chapterRecyclerView.adapter =
        ChapterSummaryAdapter(
          storySummaryViewModel.storySummary.storyId,
          chapterList,
          chapterSummarySelector
        )

      binding.root.setOnClickListener {
        val previousIndex: Int? = currentExpandedChapterListIndex
        currentExpandedChapterListIndex =
          if (currentExpandedChapterListIndex != null &&
            currentExpandedChapterListIndex == position
          ) {
            null
          } else {
            position
          }
        expandedChapterListIndexListener.onExpandListIconClicked(currentExpandedChapterListIndex)
        if (previousIndex != null && currentExpandedChapterListIndex != null &&
          previousIndex == currentExpandedChapterListIndex
        ) {
          notifyItemChanged(currentExpandedChapterListIndex!!)
        } else {
          if (previousIndex != null) {
            notifyItemChanged(previousIndex)
          }
          if (currentExpandedChapterListIndex != null) {
            notifyItemChanged(currentExpandedChapterListIndex!!)
          }
        }
      }
    }
  }
}
