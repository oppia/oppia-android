package org.oppia.app.topic.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.app.topic.TOPIC_ID_ARGUMENT_KEY
import javax.inject.Inject

/** Fragment that contains info of Topic. */
class TopicInfoFragment : InjectableFragment() {
  @Inject
  lateinit var topicInfoFragmentPresenter: TopicInfoFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getString(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicInfoFragment."
    }
    return topicInfoFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId
    )
  }
}
