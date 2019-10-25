package org.oppia.app.topic.play

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter
import androidx.databinding.library.baseAdapters.BR
import org.oppia.app.databinding.PlayChapterViewBinding
import org.oppia.app.model.ChapterSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

/** Adapter to bind ChapterSummary to [RecyclerView] inside [TopicPlayFragment]. */
class ChapterSelectionAdapter(private val chapterList: List<ChapterSummary>) : BaseAdapter() {
  @SuppressLint("ViewHolder")
  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
    val chapterSummaryListItemBinding = DataBindingUtil.inflate<PlayChapterViewBinding>(
      LayoutInflater.from(parent!!.context),
      R.layout.play_chapter_view, parent,
      /* attachToRoot= */ false
    )

    chapterSummaryListItemBinding.setVariable(BR.chapter, chapterList[position])
    return chapterSummaryListItemBinding.root
  }

  override fun getItem(position: Int): Any {
    return position
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getCount(): Int {
    return chapterList.size
  }
}
