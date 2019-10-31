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
  @Inject
  lateinit var topicFragmentPresenter: TopicFragmentPresenter
  private var topicId: String = ""

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    topicId = if (arguments != null && arguments!!.getString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY) != null)
      arguments!!.getString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY) else ""
    return topicFragmentPresenter.handleCreateView(inflater, container, topicId)
  }
}
