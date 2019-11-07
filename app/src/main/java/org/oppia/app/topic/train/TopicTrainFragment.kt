package org.oppia.app.topic.train

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_SKILL_ID_LIST = "SKILL_ID_LIST"

/** Fragment that displays skills for topic train mode. */
class TopicTrainFragment : InjectableFragment() {
  @Inject
  lateinit var topicTrainFragmentPresenter: TopicTrainFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    var selectedIdList = ArrayList<String>()
    if (savedInstanceState != null) {
      selectedIdList = savedInstanceState.getStringArrayList(KEY_SKILL_ID_LIST)
    }
    return topicTrainFragmentPresenter.handleCreateView(inflater, container, selectedIdList)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putStringArrayList(KEY_SKILL_ID_LIST, topicTrainFragmentPresenter.selectedSkillIdList)
  }
}
