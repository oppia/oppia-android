package org.oppia.android.app.topicdownloaded

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains topic downloaded view. */
class TopicDownloadedFragment : InjectableFragment() {

  @Inject
  lateinit var topicDownloadedFragmentPresenter: TopicDownloadedFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt("id", -1)!!
    val topicId = arguments?.getString("topicId")!!
    return topicDownloadedFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId
    )
  }
}
