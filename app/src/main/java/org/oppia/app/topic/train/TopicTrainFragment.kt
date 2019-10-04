package org.oppia.app.topic.train

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains skills for topic train mode. */
class TopicTrainFragment : InjectableFragment() {
  @Inject
  lateinit var topicTrainFragmentPresenter: TopicTrainFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return topicTrainFragmentPresenter.handleCreateView(inflater, container)
  }

  fun submitButtonClicked() {
    val skillList = topicTrainFragmentPresenter.getSelectedSkillList()
    val questionPlayerIntent = Intent()
    questionPlayerIntent.putStringArrayListExtra("SKILL_LIST", skillList)
    startActivity(questionPlayerIntent)
  }
}
