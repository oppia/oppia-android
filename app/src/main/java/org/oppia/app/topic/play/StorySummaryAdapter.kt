package org.oppia.app.topic.play

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.TopicPlayStorySummaryBinding
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.StorySummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind StorySummary to [RecyclerView] inside [TopicPlayFragment]. */
class StorySummaryAdapter(
  private var storyList: MutableList<StorySummary>,
  private val storySummarySelector: StorySummarySelector
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
    internal fun bind(storySummary: StorySummary, @Suppress("UNUSED_PARAMETER") position: Int) {
      binding.setVariable(BR.storySummary, storySummary)

      val totalChapterCount = storySummary.chapterCount
      val chapterSummaries = storySummary.chapterList
      val completedChapterCount =
        chapterSummaries.map(ChapterSummary::getChapterPlayState)
          .filter {
            it == ChapterPlayState.COMPLETED
          }
          .size

      val chapterList = storySummary.chapterList
      binding.chapterRecyclerView.adapter = ChapterSummaryAdapter(chapterList)

      val storyProgressPercentage: Int = (completedChapterCount * 100 / totalChapterCount)
      binding.setVariable(BR.storyProgressPercentage, storyProgressPercentage)

      binding.storyNameTextView.setOnClickListener {
        storySummarySelector.selectedStorySummary(storySummary)
      }
    }
  }
}
