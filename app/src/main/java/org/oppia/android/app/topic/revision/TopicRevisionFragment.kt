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
import org.oppia.android.app.model.TopicRevisionFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that card for topic revision. */
class TopicRevisionFragment : InjectableFragment(), RevisionSubtopicSelector {
  companion object {
    /** Arguments key for TopicRevisionFragment. */
    const val TOPIC_REVISION_FRAGMENT_ARGUMENTS_KEY = "TopicRevisionFragment.arguments"

    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val TOPIC_REVISION_FRAGMENT_TAG = "TOPIC_REVISION_FRAGMENT_TAG"

    /** Returns a new [TopicRevisionFragment]. */
    fun newInstance(profileId: ProfileId, topicId: String): TopicRevisionFragment {
      val args = TopicRevisionFragmentArguments.newBuilder().setTopicId(topicId).build()
      return TopicRevisionFragment().apply {
        arguments = Bundle().apply {
          putProto(TOPIC_REVISION_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
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
    val profileId = arguments?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()

    val args = arguments?.getProto(
      TOPIC_REVISION_FRAGMENT_ARGUMENTS_KEY,
      TopicRevisionFragmentArguments.getDefaultInstance()
    )

    val topicId = checkNotNull(args?.topicId) {
      "Expected topic ID to be included in arguments for TopicRevisionFragment."
    }
    return topicReviewFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId
    )
  }

  override fun onTopicRevisionSummaryClicked(subtopic: Subtopic) {
    topicReviewFragmentPresenter.onTopicRevisionSummaryClicked(subtopic)
  }
}
