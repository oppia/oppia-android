package org.oppia.app.topic.train

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.topic_train_skill_view.view.*
import org.oppia.app.R
import org.oppia.app.databinding.TopicTrainSkillViewBinding
import org.oppia.app.model.SkillSummary

// TODO(#172): Make use of generic data-binding-enabled RecyclerView adapter.
/** Adapter to bind skills to [RecyclerView] inside [TopicTrainFragment]. */
class SkillSelectionAdapter(private val skillSelector: SkillSelector) :
  RecyclerView.Adapter<SkillSelectionAdapter.SkillViewHolder>() {

  private var skillList: List<SkillSummary> = ArrayList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
    val skillListItemBinding = DataBindingUtil.inflate<TopicTrainSkillViewBinding>(
      LayoutInflater.from(parent.context),
      R.layout.topic_train_skill_view, parent, false
    )
    return SkillViewHolder(skillListItemBinding)
  }

  override fun onBindViewHolder(skillViewHolder: SkillViewHolder, i: Int) {
    val skill = skillList[i]
    skillViewHolder.bind(skill, i)
  }

  override fun getItemCount(): Int {
    return skillList.size
  }

  fun setSkillList(skillList: List<SkillSummary>) {
    this.skillList = skillList
    notifyDataSetChanged()
  }

  inner class SkillViewHolder(val binding: TopicTrainSkillViewBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: SkillSummary, position: Int) {
      binding.setVariable(BR.skill, rawString)
      binding.root.skill_check_box.setOnCheckedChangeListener { buttonView, isChecked ->
        val skill = skillList[position]
        if (isChecked) {
          skillSelector.skillSelected(skill)
        } else {
          skillSelector.skillUnselected(skill)
        }
      }
    }
  }
}
