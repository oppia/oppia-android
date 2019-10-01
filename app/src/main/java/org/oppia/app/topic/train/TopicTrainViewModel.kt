package org.oppia.app.topic.train

import android.util.Log
import android.widget.CompoundButton
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData
import kotlin.collections.ArrayList

/** [ViewModel] for showing skills in train fragment. */
@FragmentScope
class TopicTrainViewModel @Inject constructor() : ViewModel(){
  var skillListLiveData: MutableLiveData<List<String>> = dummySkillListLiveData()

  var isSubmitButtonActive = ObservableField<Boolean>(false)

  private fun dummySkillListLiveData(): MutableLiveData<List<String>> {
    skillListLiveData = MutableLiveData()

    val skillList = ArrayList<String>()
    skillList.add("Identify the Parts of a Fraction")
    skillList.add("Writing Fractions")
    skillList.add("Equivalent Fractions")
    skillList.add("Mixed Numbers and Improper Fractions")
    skillList.add("Comparing Fractions")
    skillList.add("Adding and Subtracting Fractions")
    skillList.add("Multiplying Fractions")
    skillList.add("Dividing Fractions")
    skillListLiveData.value = skillList
    return skillListLiveData
  }

  fun onCheckChanged(compoundButton: CompoundButton, isChecked: Boolean){
    Log.d("TAG","adfs")
  }
}
