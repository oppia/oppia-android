package org.oppia.app.topic.train

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SkillSummary
import javax.inject.Inject

/** [ViewModel] for showing skills in train fragment. */
@FragmentScope
class TopicTrainViewModel @Inject constructor() : ViewModel() {
  var isSubmitButtonActive = ObservableField<Boolean>(false)

  fun selectedSkillList(selectedSkillList: ArrayList<SkillSummary>) {
    isSubmitButtonActive.set(selectedSkillList.isNotEmpty())
  }
}
