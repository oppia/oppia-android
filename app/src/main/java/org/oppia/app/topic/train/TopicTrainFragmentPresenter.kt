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
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicTrainFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.skillRecyclerView.apply {
      adapter = SkillSelectionAdapter(context, dummySkillList())
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
}
