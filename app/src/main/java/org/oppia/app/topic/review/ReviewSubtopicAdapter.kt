package org.oppia.app.topic.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.TopicReviewSummaryViewBinding
import org.oppia.app.model.Subtopic

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind skills to [RecyclerView] inside [TopicReviewFragment]. */
class ReviewSubtopicAdapter(private val reviewSelector: ReviewSubtopicSelector) :
  RecyclerView.Adapter<ReviewSubtopicAdapter.SubtopicViewHolder>() {

  private var subtopicList: List<Subtopic> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtopicViewHolder {
    val reviewListItemBinding = DataBindingUtil.inflate<TopicReviewSummaryViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_review_summary_view, parent,
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

  fun setReviewList(subtopicList: List<Subtopic>) {
    this.subtopicList = subtopicList
    notifyDataSetChanged()
  }

  inner class SubtopicViewHolder(val binding: TopicReviewSummaryViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(subtopic: Subtopic) {
      binding.setVariable(BR.subtopic, subtopic)
      binding.root.setOnClickListener {
        reviewSelector.onTopicReviewSummaryClicked(subtopic)
      }
    }
  }
}
