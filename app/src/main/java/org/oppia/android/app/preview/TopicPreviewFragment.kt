package org.oppia.android.app.preview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains topic preview. */
class TopicPreviewFragment : InjectableFragment(), TopicPreviewListener {

  @Inject
  lateinit var topicPreviewFragmentPresenter: TopicPreviewFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY)!!
    val topicId = arguments?.getString(TOPIC_ID_ARGUMENT_KEY)!!
    return topicPreviewFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId
    )
  }

  override fun onDownloadTopicClicked() {
    topicPreviewFragmentPresenter.showDownloadedTopic()
  }
}
