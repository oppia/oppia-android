package org.oppia.app.topic.revision

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.TopicRevisionSummaryViewBinding
import org.oppia.app.model.Subtopic

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind skills to [RecyclerView] inside [TopicRevisionFragment]. */
class RevisionSubtopicAdapter(private val revisionSelector: RevisionSubtopicSelector) :
  RecyclerView.Adapter<RevisionSubtopicAdapter.SubtopicViewHolder>() {

  private var subtopicList: List<Subtopic> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtopicViewHolder {
    val reviewListItemBinding = DataBindingUtil.inflate<TopicRevisionSummaryViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_revision_summary_view, parent,
      /* attachToRoot= */ false
    )
    return SubtopicViewHolder(reviewListItemBinding)
  }

  override fun onBindViewHolder(subtopicViewHolder: SubtopicViewHolder, i: Int) {
    subtopicViewHolder.bind(subtopicList[i])
  }

  override fun getItemCount(): Int {
    return subtopicList.size
  }

  fun setRevisionList(subtopicList: List<Subtopic>) {
    this.subtopicList = subtopicList
    notifyDataSetChanged()
  }

  inner class SubtopicViewHolder(val binding: TopicRevisionSummaryViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(subtopic: Subtopic) {
      binding.setVariable(BR.subtopic, subtopic)
      binding.root.setOnClickListener {
        revisionSelector.onTopicRevisionSummaryClicked(subtopic)
      }
    }
  }
}
