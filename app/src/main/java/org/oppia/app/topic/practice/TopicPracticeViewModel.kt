package org.oppia.app.topic.practice

import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.SkillSummary
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for showing skills in practice fragment. */
@FragmentScope
class TopicPracticeViewModel @Inject constructor(
  private val topicPracticeFragmentPresenter: TopicPracticeFragmentPresenter,
  private val logger: Logger
  ) : ObservableViewModel() {

  val skillListLiveData : ObservableList<SkillSummary> = ObservableArrayList()
  fun setSkillList(skillList: List<SkillSummary>) {
    skillListLiveData.addAll(skillList)
  }

  var isSubmitButtonActive = ObservableField<Boolean>(false)

  fun notifySelectedSkillList(selectedSkillList: ArrayList<String>) {
    isSubmitButtonActive.set(selectedSkillList.isNotEmpty())
  }

  fun startButtonClicked(v: View) {
    topicPracticeFragmentPresenter.onStartButtonClicked()
  }
}
