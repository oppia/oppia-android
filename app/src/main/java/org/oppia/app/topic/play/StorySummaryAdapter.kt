package org.oppia.app.topic.play

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.customview.CustomProgressView
import org.oppia.app.databinding.TopicPlayStorySummaryBinding
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.StorySummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind StorySummary to [RecyclerView] inside [TopicPlayFragment]. */
class StorySummaryAdapter(
  private var storyList: MutableList<StorySummary>,
  private val chapterSummarySelector: ChapterSummarySelector,
  private val storySummarySelector: StorySummarySelector,
  private val expandedChapterListIndexListener: ExpandedChapterListIndexListener,
  private var currentExpandedChapterListIndex: Int?
) :
  RecyclerView.Adapter<StorySummaryAdapter.StorySummaryViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorySummaryViewHolder {
    val storySummaryListItemBinding = DataBindingUtil.inflate<TopicPlayStorySummaryBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_play_story_summary, parent,
      /* attachToRoot= */ false
    )
    return StorySummaryViewHolder(storySummaryListItemBinding)
  }

  override fun onBindViewHolder(storySummaryViewHolder: StorySummaryViewHolder, i: Int) {
    storySummaryViewHolder.bind(storyList[i], i)
  }

  override fun getItemCount(): Int {
    return storyList.size
  }

  inner class StorySummaryViewHolder(private val binding: TopicPlayStorySummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(storySummary: StorySummary, position: Int) {
      var isChapterListVisible = false
      if (currentExpandedChapterListIndex != null) {
        isChapterListVisible = currentExpandedChapterListIndex!! == position
      }
      binding.isListExpanded = isChapterListVisible
      binding.storySummary = storySummary

      val chapterSummaries = storySummary.chapterList
      val completedChapterCount =
        chapterSummaries.map(ChapterSummary::getChapterPlayState)
          .filter {
            it == ChapterPlayState.COMPLETED
          }
          .size
      val storyPercentage: Int = (completedChapterCount * 100) / storySummary.chapterCount
      binding.storyPercentage = storyPercentage
      binding.storyProgressView.setStoryChapterDetails(storySummary.chapterCount, completedChapterCount)

      val chapterList = storySummary.chapterList
      binding.chapterRecyclerView.adapter = ChapterSummaryAdapter(chapterList, chapterSummarySelector)

      binding.storyNameTextView.setOnClickListener {
        storySummarySelector.selectStorySummary(storySummary)
      }

      binding.chapterListViewControl.setOnClickListener {
        val previousIndex: Int? = currentExpandedChapterListIndex
        currentExpandedChapterListIndex =
          if (currentExpandedChapterListIndex != null && currentExpandedChapterListIndex == position) {
            null
          } else {
            position
          }
        expandedChapterListIndexListener.onExpandListIconClicked(currentExpandedChapterListIndex)

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
