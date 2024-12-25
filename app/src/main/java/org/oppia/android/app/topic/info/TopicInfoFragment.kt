package org.oppia.android.app.topic.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicInfoFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that contains info of Topic. */
class TopicInfoFragment : InjectableFragment() {
  companion object {

    /** Arguments key for TopicInfoFragment. */
    const val TOPIC_INFO_FRAGMENT_ARGUMENTS_KEY = "TopicInfoFragment.arguments"

    /** Returns a new [TopicInfoFragment]. */
    fun newInstance(profileId: ProfileId, topicId: String): TopicInfoFragment {
      val args = TopicInfoFragmentArguments.newBuilder().setTopicId(topicId).build()

      return TopicInfoFragment().apply {
        arguments = Bundle().apply {
          putProto(TOPIC_INFO_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }

  @Inject
  lateinit var topicInfoFragmentPresenter: TopicInfoFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = arguments?.getProto(
      TOPIC_INFO_FRAGMENT_ARGUMENTS_KEY,
      TopicInfoFragmentArguments.getDefaultInstance()
    )

    val profileId = arguments?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()

    val topicId = checkNotNull(args?.topicId) {
      "Expected topic ID to be included in arguments for TopicInfoFragment."
    }
    return topicInfoFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId
    )
  }
}
