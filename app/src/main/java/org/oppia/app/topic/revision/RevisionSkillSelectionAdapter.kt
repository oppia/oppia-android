package org.oppia.app.topic.revision

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.TopicRevisionSummaryViewBinding
import org.oppia.app.model.SkillSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind skills to [RecyclerView] inside [TopicRevisionFragment]. */
class RevisionSkillSelectionAdapter(private val revisionSkillSelector: RevisionSkillSelector) :
  RecyclerView.Adapter<RevisionSkillSelectionAdapter.SkillViewHolder>() {

  private var skillList: List<SkillSummary> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
    val skillListItemBinding = DataBindingUtil.inflate<TopicRevisionSummaryViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_revision_summary_view, parent,
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

  inner class SkillViewHolder(val binding: TopicRevisionSummaryViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(skill: SkillSummary, @Suppress("UNUSED_PARAMETER") position: Int) {
      binding.setVariable(BR.skill, skill)
      binding.root.setOnClickListener {
        revisionSkillSelector.onTopicRevisionSummaryClicked(skill)
      }
    }
  }
}
