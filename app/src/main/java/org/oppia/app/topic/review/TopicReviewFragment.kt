package org.oppia.app.topic.review

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.Subtopic
import javax.inject.Inject

/** Fragment that card for topic review. */
class TopicReviewFragment : InjectableFragment(), ReviewSubtopicSelector {
  @Inject lateinit var topicReviewFragmentPresenter: TopicReviewFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return topicReviewFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onTopicReviewSummaryClicked(subtopic: Subtopic) {
    topicReviewFragmentPresenter.onTopicReviewSummaryClicked(subtopic)
  }
}
