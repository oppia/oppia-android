package org.oppia.app.topic.practice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.topic_practice_skill_view.view.*
import org.oppia.app.R
import org.oppia.app.databinding.TopicPracticeSkillViewBinding
import org.oppia.app.model.SkillSummary

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind skills to [RecyclerView] inside [TopicPracticeFragment]. */
class SkillSelectionAdapter(private val skillSelector: SkillSelector) :
  RecyclerView.Adapter<SkillSelectionAdapter.SkillViewHolder>() {

  private var skillList: List<SkillSummary> = ArrayList()
  private var selectedSkillIdList: ArrayList<String> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
    val skillListItemBinding = DataBindingUtil.inflate<TopicPracticeSkillViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_practice_skill_view, parent,
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

  fun setSelectedSkillList(skillIdList: ArrayList<String>) {
    selectedSkillIdList = skillIdList
  }

  inner class SkillViewHolder(val binding: TopicPracticeSkillViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(skill: SkillSummary, position: Int) {
      binding.setVariable(BR.isChecked, selectedSkillIdList.contains(skill.skillId))
      binding.setVariable(BR.skill, skill)
      binding.root.skill_check_box.setOnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
          skillSelector.skillSelected(skill.skillId)
        } else {
          skillSelector.skillUnselected(skill.skillId)
        }
      }
    }
  }
}
