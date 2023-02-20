package org.oppia.android.app.topic.info

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
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that contains info of Topic. */
class TopicInfoFragment : InjectableFragment() {
  companion object {
    /** Returns a new [TopicInfoFragment]. */
    fun newInstance(profileId: ProfileId, topicId: String): TopicInfoFragment {
      val topicInfoFragment = TopicInfoFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicInfoFragment.arguments = args
      return topicInfoFragment
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
    val profileId = arguments?.extractCurrentUserProfileId()
      ?: ProfileId.newBuilder().apply { internalId = -1 }.build()
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
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
