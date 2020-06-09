package org.oppia.app.topic.practice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import javax.inject.Inject

private const val KEY_SKILL_ID_LIST = "SKILL_ID_LIST"

/** Fragment that displays skills for topic practice mode. */
class TopicPracticeFragment : InjectableFragment() {
  companion object {
    /** Returns a new [TopicPracticeFragment] */
    fun newInstance(internalProfileId: Int, topicId: String): TopicPracticeFragment {
      val topicPracticeFragment = TopicPracticeFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicPracticeFragment.arguments = args
      return topicPracticeFragment
    }
  }

  @Inject lateinit var topicPracticeFragmentPresenter: TopicPracticeFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    var selectedIdList = ArrayList<String>()
    if (savedInstanceState != null) {
      selectedIdList = savedInstanceState.getStringArrayList(KEY_SKILL_ID_LIST)
    }
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicPracticeFragment."
    }
    return topicPracticeFragmentPresenter.handleCreateView(
      inflater,
      container,
      selectedIdList,
      internalProfileId,
      topicId
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putStringArrayList(KEY_SKILL_ID_LIST, topicPracticeFragmentPresenter.selectedSkillIdList)
  }
}
