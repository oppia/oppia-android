package org.oppia.android.app.topic.practice

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicPracticeFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that displays skills for topic practice mode. */
class TopicPracticeFragment : InjectableFragment() {
  companion object {
    /** Arguments key for TopicPracticeFragment. */
    const val TOPIC_PRACTICE_FRAGMENT_ARGUMENTS_KEY = "TopicPracticeFragment.arguments"

    internal const val SUBTOPIC_ID_LIST_ARGUMENT_KEY = "TopicPracticeFragment.subtopic_id_list"
    internal const val SKILL_ID_LIST_ARGUMENT_KEY = "TopicPracticeFragment.skill_id_list"

    /** Returns a new [TopicPracticeFragment]. */
    fun newInstance(internalProfileId: Int, topicId: String): TopicPracticeFragment {

      Log.e("#", internalProfileId.toString() + " " + topicId)
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      val args = TopicPracticeFragmentArguments.newBuilder().apply {
        this.topicId = topicId
      }.build()
      return TopicPracticeFragment().apply {
        arguments = Bundle().apply {
          putProto(TOPIC_PRACTICE_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
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
    val args = arguments?.getProto(
      TOPIC_PRACTICE_FRAGMENT_ARGUMENTS_KEY,
      TopicPracticeFragmentArguments.getDefaultInstance()
    )
    val internalProfileId = arguments?.extractCurrentUserProfileId()?.internalId ?: -1
    val topicId = checkNotNull(args?.topicId) {
      "Expected topic ID to be included in arguments for TopicPracticeFragment."
    }

    return topicPracticeFragmentPresenter.handleCreateView(
      inflater,
      container,
      selectedIdList,
      selectedSkillId,
      internalProfileId,
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
