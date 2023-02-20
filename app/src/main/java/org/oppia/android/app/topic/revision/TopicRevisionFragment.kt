package org.oppia.android.app.topic.revision

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Fragment that card for topic revision. */
class TopicRevisionFragment : InjectableFragment(), RevisionSubtopicSelector {
  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val TOPIC_REVISION_FRAGMENT_TAG = "TOPIC_REVISION_FRAGMENT_TAG"
    /** Returns a new [TopicRevisionFragment]. */
    fun newInstance(profileId: ProfileId, topicId: String): TopicRevisionFragment {
      val topicRevisionFragment = TopicRevisionFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicRevisionFragment.arguments = args
      return topicRevisionFragment
    }
  }

  @Inject
  lateinit var topicReviewFragmentPresenter: TopicRevisionFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicRevisionFragment."
    }
    return topicReviewFragmentPresenter.handleCreateView(
      inflater,
      container,
      topicId
    )
  }

  override fun onTopicRevisionSummaryClicked(subtopic: Subtopic) {
    topicReviewFragmentPresenter.onTopicRevisionSummaryClicked(subtopic)
  }
}
