package org.oppia.app.topic.train

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.TopicTrainFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [TopicTrainFragment]. */
@FragmentScope
class TopicTrainFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicTrainViewModel>
) : SkillInterface {
  private val selectedSkillList = ArrayList<String>()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val skillAdapter = SkillSelectionAdapter(dummySkillList(), this)

    val binding = TopicTrainFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.skillRecyclerView.apply {
      adapter = skillAdapter
      layoutManager = LinearLayoutManager(context)
    }
    binding.let {
      it.viewModel = getTopicTrainViewModel()
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getTopicTrainViewModel(): TopicTrainViewModel {
    return viewModelProvider.getForFragment(fragment, TopicTrainViewModel::class.java)
  }

  override fun skillSelected(skill: String) {
    selectedSkillList.add(skill)
    getTopicTrainViewModel().selectedSkillList(selectedSkillList)
  }

  override fun skillUnselected(skill: String) {
    selectedSkillList.remove(skill)
    getTopicTrainViewModel().selectedSkillList(selectedSkillList)
  }

  private fun dummySkillList(): List<String> {
    val skillList = ArrayList<String>()
    skillList.add("Identify the Parts of a Fraction")
    skillList.add("Writing Fractions")
    skillList.add("Equivalent Fractions")
    skillList.add("Mixed Numbers and Improper Fractions")
    skillList.add("Comparing Fractions")
    skillList.add("Adding and Subtracting Fractions")
    skillList.add("Multiplying Fractions")
    skillList.add("Dividing Fractions")
    return skillList
  }

  fun getSelectedSkillList() = selectedSkillList
}
