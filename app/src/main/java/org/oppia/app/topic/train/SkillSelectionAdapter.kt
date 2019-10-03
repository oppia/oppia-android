package org.oppia.app.topic.train;

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.topic_train_skill_view.view.*
import org.oppia.app.R
import org.oppia.app.databinding.TopicTrainSkillViewBinding

/** Adapter to bind skills to [RecyclerView] inside [TopicTrainFragment]. **/
class SkillSelectionAdapter(
  private val skillList: List<String>,
  private val skillInterface: SkillInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding =
      DataBindingUtil.inflate<TopicTrainSkillViewBinding>(
        inflater,
        R.layout.topic_train_skill_view,
        parent,
        false
      )
    return SkillViewHolder(binding)
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as SkillViewHolder).bind(skillList[position], position)
  }

  override fun getItemCount(): Int {
    return skillList.size
  }

  private inner class SkillViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(rawString: String?, position: Int) {
      binding.setVariable(BR.skill, rawString)
      binding.root.skill_check_box.setOnCheckedChangeListener { buttonView, isChecked ->
        val skill = skillList[position]
        if (isChecked) {
          skillInterface.skillSelected(skill)
        } else {
          skillInterface.skillUnselected(skill)
        }
      }
    }
  }
}
