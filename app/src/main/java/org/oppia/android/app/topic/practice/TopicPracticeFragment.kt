package org.oppia.android.app.topic.practice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicPracticeFragmentArguments
import org.oppia.android.app.model.TopicPracticeFragmentStateBundle
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

    /** State key for TopicPracticeFragment. */
    const val TOPIC_PRACTICE_FRAGMENT_STATE_KEY = "TopicPracticeFragment.state"

    /** Returns a new [TopicPracticeFragment]. */
    fun newInstance(profileId: ProfileId, topicId: String): TopicPracticeFragment {
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
      val savedArgs = savedInstanceState.getProto(
        TOPIC_PRACTICE_FRAGMENT_STATE_KEY,
        TopicPracticeFragmentStateBundle.getDefaultInstance()
      )
      selectedIdList = ArrayList(savedArgs.subtopicIdsList)
      selectedSkillId = savedArgs.skillIdsMap.mapValues { entry ->
        entry.value.valuesList.toMutableList()
      } as HashMap<Int, MutableList<String>>
    }
    val args = arguments?.getProto(
      TOPIC_PRACTICE_FRAGMENT_ARGUMENTS_KEY,
      TopicPracticeFragmentArguments.getDefaultInstance()
    )
    val profileId = arguments?.extractCurrentUserProfileId() ?: ProfileId.newBuilder()
      .setInternalId(-1).build()
    val topicId = checkNotNull(args?.topicId) {
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
    val args = TopicPracticeFragmentStateBundle.newBuilder().apply {
      this.addAllSubtopicIds(topicPracticeFragmentPresenter.selectedSubtopicIdList)
      topicPracticeFragmentPresenter.skillIdHashMap.forEach { (key, value) ->
        this.putSkillIds(
          key,
          TopicPracticeFragmentStateBundle.StringList.newBuilder().addAllValues(value).build()
        )
      }
    }.build()
    outState.putProto(TOPIC_PRACTICE_FRAGMENT_STATE_KEY, args)
  }
}
