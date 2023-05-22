package org.oppia.android.app.topic.practice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

// TODO(#4986): Remove the constants corresponding to bundles.
private const val PROFILE_ID_ARGUMENT_KEY = "profile_id"
private const val TOPIC_ID_ARGUMENT_KEY = "topic_id"

/** Fragment that displays skills for topic practice mode. */
class TopicPracticeFragment : InjectableFragment() {
  companion object {
    internal const val SUBTOPIC_ID_LIST_ARGUMENT_KEY = "TopicPracticeFragment.subtopic_id_list"
    internal const val SKILL_ID_LIST_ARGUMENT_KEY = "TopicPracticeFragment.skill_id_list"

    /** Returns a new [TopicPracticeFragment]. */
    fun newInstance(internalProfileId: Int, topicId: String): TopicPracticeFragment {
      val topicPracticeFragment = TopicPracticeFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicPracticeFragment.arguments = args
      return topicPracticeFragment
    }
  }

  @Inject
  lateinit var topicPracticeFragmentPresenter: TopicPracticeFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
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
      // TODO(#4437): Convert this to a type-safe proto.
      @Suppress("UNCHECKED_CAST") // Not quite safe.
      selectedSkillId = savedInstanceState
        .getSerializable(SKILL_ID_LIST_ARGUMENT_KEY)!! as HashMap<Int, MutableList<String>>
    }
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
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

  /** Dagger injector for [TopicPracticeFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: TopicPracticeFragment)
  }
}
