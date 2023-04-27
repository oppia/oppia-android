package org.oppia.android.app.topic.revision

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.Subtopic
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

// TODO: Consolidate these up with the ones in TopicActivityPresenter & clean up.
private const val PROFILE_ID_ARGUMENT_KEY = "profile_id"
private const val TOPIC_ID_ARGUMENT_KEY = "topic_id"

/** Fragment that card for topic revision. */
class TopicRevisionFragment : InjectableFragment(), RevisionSubtopicSelectionListener {
  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val TOPIC_REVISION_FRAGMENT_TAG = "TOPIC_REVISION_FRAGMENT_TAG"
    /** Returns a new [TopicRevisionFragment]. */
    fun newInstance(internalProfileId: Int, topicId: String): TopicRevisionFragment {
      val topicRevisionFragment = TopicRevisionFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicRevisionFragment.arguments = args
      return topicRevisionFragment
    }
  }

  @Inject
  lateinit var topicReviewFragmentPresenter: TopicRevisionFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicRevisionFragment."
    }
    return topicReviewFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId
    )
  }

  override fun onTopicRevisionSummaryClicked(subtopic: Subtopic) {
    topicReviewFragmentPresenter.onTopicRevisionSummaryClicked(subtopic)
  }

  interface Injector {
    fun inject(fragment: TopicRevisionFragment)
  }
}
