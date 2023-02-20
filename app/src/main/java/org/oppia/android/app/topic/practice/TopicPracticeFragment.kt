package org.oppia.android.app.topic.practice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that displays skills for topic practice mode. */
class TopicPracticeFragment : InjectableFragment() {
  companion object {
    internal const val SUBTOPIC_ID_LIST_ARGUMENT_KEY = "TopicPracticeFragment.subtopic_id_list"
    internal const val SKILL_ID_LIST_ARGUMENT_KEY = "TopicPracticeFragment.skill_id_list"

    /** Returns a new [TopicPracticeFragment]. */
    fun newInstance(profileId: ProfileId, topicId: String): TopicPracticeFragment {
      val topicPracticeFragment = TopicPracticeFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicPracticeFragment.arguments = args
      return topicPracticeFragment
    }
  }

  @Inject
  lateinit var topicPracticeFragmentPresenter: TopicPracticeFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    var selectedIdList = ArrayList<Int>()
    var selectedSkillId = HashMap<Int, MutableList<String>>()
    if (savedInstanceState != null) {
      selectedIdList = savedInstanceState.getIntegerArrayList(SUBTOPIC_ID_LIST_ARGUMENT_KEY)!!
      selectedSkillId = savedInstanceState
        .getSerializable(SKILL_ID_LIST_ARGUMENT_KEY)!! as HashMap<Int, MutableList<String>>
    }
    val profileId = arguments?.extractCurrentUserProfileId()
      ?: ProfileId.newBuilder().apply { internalId = -1 }.build()
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicPracticeFragment."
    }
    return topicPracticeFragmentPresenter.handleCreateView(
      inflater,
      container,
      selectedIdList,
      selectedSkillId,
      profileId,
      topicId
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putIntegerArrayList(
      SUBTOPIC_ID_LIST_ARGUMENT_KEY,
      topicPracticeFragmentPresenter.selectedSubtopicIdList
    )
    outState.putSerializable(
      SKILL_ID_LIST_ARGUMENT_KEY,
      topicPracticeFragmentPresenter.skillIdHashMap
    )
  }
}
