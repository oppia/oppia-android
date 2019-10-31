package org.oppia.app.topic.play

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import org.oppia.app.databinding.PlayChapterViewBinding
import org.oppia.app.model.ChapterSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind ChapterSummary to [RecyclerView] inside [TopicPlayFragment]. */
class ChapterSummaryAdapter(
  private val chapterList: List<ChapterSummary>,
  private val chapterSummarySelector: ChapterSummarySelector
) :
  RecyclerView.Adapter<ChapterSummaryAdapter.ChapterSummaryViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterSummaryViewHolder {
    val chapterSummaryListItemBinding = DataBindingUtil.inflate<PlayChapterViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.play_chapter_view, parent,
      /* attachToRoot= */ false
    )
    return ChapterSummaryViewHolder(chapterSummaryListItemBinding)
  }

  override fun getItemCount(): Int {
    return chapterList.size
  }

  override fun onBindViewHolder(chapterSummaryViewHolder: ChapterSummaryViewHolder, position: Int) {
    chapterSummaryViewHolder.bind(chapterList[position])
  }

  inner class ChapterSummaryViewHolder(private val binding: PlayChapterViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(chapterSummary: ChapterSummary) {
      binding.chapterSummary = chapterSummary
      binding.chapterName.setOnClickListener {
        chapterSummarySelector.selectChapterSummary(chapterSummary)
      }
    }
  }
}
