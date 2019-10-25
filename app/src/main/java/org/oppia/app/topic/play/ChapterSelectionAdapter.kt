package org.oppia.app.topic.play

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import androidx.databinding.library.baseAdapters.BR
import org.oppia.app.databinding.PlayChapterViewBinding
import org.oppia.app.model.ChapterSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind chapters to [RecyclerView] inside [TopicPlayFragment]. */
class ChapterSelectionAdapter(private val chapterList: List<ChapterSummary>) : RecyclerView.Adapter<ChapterSelectionAdapter.ChapterSummaryViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterSummaryViewHolder {
    Log.d("TAG","onCreateViewHolder")
    val chapterSummaryListItemBinding = DataBindingUtil.inflate<PlayChapterViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.play_chapter_view, parent,
      /* attachToRoot= */ false
    )
    return ChapterSummaryViewHolder(chapterSummaryListItemBinding)
  }

  override fun onBindViewHolder(chapterSummaryViewHolder: ChapterSummaryViewHolder, i: Int) {
    Log.d("TAG","onBindViewHolder")
    chapterSummaryViewHolder.bind(chapterList[i], i)
  }

  override fun getItemCount(): Int {
    Log.d("TAG","getItemCount: " + chapterList.size)
    return chapterList.size
  }

  inner class ChapterSummaryViewHolder(private val binding: PlayChapterViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(chapterSummary: ChapterSummary, @Suppress("UNUSED_PARAMETER") position: Int) {
      Log.d("TAG","bind")
      binding.setVariable(BR.chapter, chapterSummary)
    }
  }
}
