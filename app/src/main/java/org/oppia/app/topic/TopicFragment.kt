package org.oppia.app.topic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains tabs for Topic. */
class TopicFragment : InjectableFragment() {
  @Inject lateinit var topicFragmentPresenter: TopicFragmentPresenter
  private lateinit var topicId: String

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    topicId = arguments!!.getString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
    return topicFragmentPresenter.handleCreateView(inflater, container,topicId)
  }
}
