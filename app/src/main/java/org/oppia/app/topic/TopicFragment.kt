package org.oppia.app.topic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

/** Fragment that contains tabs for Topic. */
class TopicFragment : InjectableFragment() {
  @Inject lateinit var topicFragmentPresenter: TopicFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY) ?: -1
    val topicId = arguments?.getString(TOPIC_ID_ARGUMENT_KEY) ?: TEST_TOPIC_ID_0
    val storyId = arguments?.getString(STORY_ID_ARGUMENT_KEY) ?: ""

    return topicFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId,
      storyId
    )
  }
}
