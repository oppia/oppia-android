package org.oppia.app.topic.review

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.domain.topic.TopicController
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/**
 * [ViewModel] for showing a list of topic summaries. Note that this can only be hosted in fragments that implement
 * [TopicReviewSummaryClickListener].
 */
@FragmentScope
class TopicReviewViewModel @Inject constructor(
  fragment: Fragment,
  private val topicListController: TopicController
) : ViewModel() {
  private val topicReviewSummaryClickListener = fragment as TopicReviewSummaryClickListener
}
