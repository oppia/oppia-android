package org.oppia.app.topic.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.TopicReviewSummaryViewBinding
import org.oppia.app.model.SkillSummary
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import androidx.databinding.BindingAdapter
import android.widget.ImageView
import org.oppia.app.R

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind skills to [RecyclerView] inside [TopicReviewFragment]. */
class ReviewSkillSelectionAdapter(private val reviewSkillSelector: ReviewSkillSelector) :
  RecyclerView.Adapter<ReviewSkillSelectionAdapter.SkillViewHolder>() {

  companion object {
    @JvmStatic
    @BindingAdapter("android:src")
    fun loadSkillThumbnail(view: ImageView, imageUrl: String) {
      val requestOptions = RequestOptions()
      requestOptions.placeholder(R.drawable.review_placeholder)

      Glide.with(view.context)
        .load(imageUrl)
        .apply(requestOptions)
        .into(view)
    }
  }

  private var skillList: List<SkillSummary> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
    val skillListItemBinding = DataBindingUtil.inflate<TopicReviewSummaryViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_review_summary_view, parent,
      /* attachToRoot= */ false
    )
    return SkillViewHolder(skillListItemBinding)
  }

  override fun onBindViewHolder(skillViewHolder: SkillViewHolder, i: Int) {
    skillViewHolder.bind(skillList[i], i)
  }

  override fun getItemCount(): Int {
    return skillList.size
  }

  fun setSkillList(skillList: List<SkillSummary>) {
    this.skillList = skillList
    notifyDataSetChanged()
  }

  inner class SkillViewHolder(val binding: TopicReviewSummaryViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(skill: SkillSummary, @Suppress("UNUSED_PARAMETER") position: Int) {
      binding.setVariable(BR.skill, skill)
      binding.root.setOnClickListener {
        reviewSkillSelector.onTopicReviewSummaryClicked(skill)
      }
    }
  }
}
