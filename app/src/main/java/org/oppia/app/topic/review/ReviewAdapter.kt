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
class ReviewAdapter(private val reviewSelector: ReviewSelector) :
  RecyclerView.Adapter<ReviewAdapter.SkillViewHolder>() {

  private var subtopicList: List<Subtopic> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
    val reviewListItemBinding = DataBindingUtil.inflate<TopicReviewSummaryViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_review_summary_view, parent,
      /* attachToRoot= */ false
    )
    return SkillViewHolder(reviewListItemBinding)
  }

  override fun onBindViewHolder(skillViewHolder: SkillViewHolder, i: Int) {
    skillViewHolder.bind(subtopicList[i], i)
  }

  override fun getItemCount(): Int {
    return subtopicList.size
  }

  fun setReviewList(subtopicList: List<Subtopic>) {
    this.subtopicList = subtopicList
    notifyDataSetChanged()
  }

  inner class SkillViewHolder(val binding: TopicReviewSummaryViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(subtopic: Subtopic, @Suppress("UNUSED_PARAMETER") position: Int) {
      binding.setVariable(BR.subtopic, subtopic)
      binding.root.setOnClickListener {
        reviewSelector.onTopicReviewSummaryClicked(subtopic)
      }
    }
  }
}
