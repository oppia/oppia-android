package org.oppia.app.topic.train

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData
import kotlin.collections.ArrayList

/** [ViewModel] for showing skills in train fragment. */
@FragmentScope
class TopicTrainViewModel @Inject constructor(
) : ViewModel() {
  var skillListSummaryLiveData: MutableLiveData<List<String>> = dummySkillListSummaryLiveData()

  private fun dummySkillListSummaryLiveData(): MutableLiveData<List<String>> {
    skillListSummaryLiveData = MutableLiveData()

    val skillList = ArrayList<String>()
    skillList.add("Identify the Parts of a Fraction")
    skillList.add("Writing Fractions")
    skillList.add("Equivalent Fractions")
    skillList.add("Mixed Numbers and Improper Fractions")
    skillList.add("Comparing Fractions")
    skillList.add("Adding and Subtracting Fractions")
    skillList.add("Multiplying Fractions")
    skillList.add("Dividing Fractions")
    skillListSummaryLiveData.value = skillList
    return skillListSummaryLiveData
  }
}
