package org.oppia.app.topic.play

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.topic.TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import javax.inject.Inject

/** Fragment that contains subtopic list for play mode. */
class TopicPlayFragment : InjectableFragment() {
  @Inject
  lateinit var topicPlayFragmentPresenter: TopicPlayFragmentPresenter
  private var topicId: String = ""

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    topicId =
      if (arguments != null && arguments!!.getString(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY) != null) arguments!!.getString(
        TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
      ) else ""
    return topicPlayFragmentPresenter.handleCreateView(inflater, container,topicId)
  }
}
