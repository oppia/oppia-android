package org.oppia.app.topic.train

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains skills for topic train mode. */
class TopicTrainFragment : InjectableFragment() {
  @Inject
  lateinit var topicTrainFragmentController: TopicTrainFragmentController

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return topicTrainFragmentController.handleCreateView(inflater, container)
  }
}
