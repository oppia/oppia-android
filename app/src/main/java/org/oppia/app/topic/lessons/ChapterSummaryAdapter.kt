package org.oppia.app.topic.lessons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.ui.R
import org.oppia.app.databinding.databinding.LessonsChapterViewBinding
import org.oppia.app.model.ChapterSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind ChapterSummary to [RecyclerView] inside [TopicLessonsFragment]. */
class ChapterSummaryAdapter(
  private val storyId: String,
  private val chapterList: List<ChapterSummary>,
  private val chapterSummarySelector: ChapterSummarySelector
) :
  RecyclerView.Adapter<ChapterSummaryAdapter.ChapterSummaryViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterSummaryViewHolder {
    val chapterSummaryListItemBinding = DataBindingUtil
      .inflate<LessonsChapterViewBinding>(
        LayoutInflater.from(parent.context),
        R.layout.lessons_chapter_view, parent,
        /* attachToRoot= */ false
      )
    return ChapterSummaryViewHolder(chapterSummaryListItemBinding)
  }

  override fun getItemCount(): Int {
    return chapterList.size
  }

  override fun onBindViewHolder(chapterSummaryViewHolder: ChapterSummaryViewHolder, position: Int) {
    chapterSummaryViewHolder.bind(chapterList[position], position)
  }

  inner class ChapterSummaryViewHolder(private val binding: LessonsChapterViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(chapterSummary: ChapterSummary, position: Int) {
      binding.chapterSummary = chapterSummary
      binding.index = position
      binding.chapterContainer.setOnClickListener {
        chapterSummarySelector.selectChapterSummary(storyId, chapterSummary)
      }
    }
  }
}
