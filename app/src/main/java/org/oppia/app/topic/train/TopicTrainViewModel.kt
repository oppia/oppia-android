package org.oppia.app.topic.train

import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for showing skills in train fragment. */
@FragmentScope
class TopicTrainViewModel @Inject constructor(
  private val topicTrainFragmentPresenter: TopicTrainFragmentPresenter
) : ViewModel() {

  var isSubmitButtonActive = ObservableField<Boolean>(false)

  fun selectedSkillList(selectedSkillList: ArrayList<String>) {
    Log.d("TAG", "Size: "+selectedSkillList.size)
    isSubmitButtonActive.set(selectedSkillList.isNotEmpty())
  }

  fun startButtonClicked(v: View) {
    topicTrainFragmentPresenter.startQuestionPlayer()
  }
}
