package org.oppia.app.topic.practice

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for showing skills in practice fragment. */
@FragmentScope
class TopicPracticeViewModel @Inject constructor(
  private val topicPracticeFragmentPresenter: TopicPracticeFragmentPresenter
) : ViewModel() {

  var isSubmitButtonActive = ObservableField<Boolean>(false)

  fun notifySelectedSkillList(selectedSkillList: ArrayList<String>) {
    isSubmitButtonActive.set(selectedSkillList.isNotEmpty())
  }

  fun startButtonClicked(v: View) {
    topicPracticeFragmentPresenter.onStartButtonClicked()
  }
}
