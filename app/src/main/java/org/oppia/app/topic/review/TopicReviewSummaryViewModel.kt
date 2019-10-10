package org.oppia.app.topic.review

import android.view.View
import org.oppia.app.model.SkillSummary

/** The view model corresponding to review skills in the topic review RecyclerView. */
class TopicReviewSummaryViewModel(
  val skillSummary: SkillSummary, private val topicReviewSummaryClickListener: TopicReviewSummaryClickListener
) {
  val name: String = skillSummary.description

  /** Callback from data-binding for when the summary tile is clicked. */
  fun clickOnReviewSummaryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    topicReviewSummaryClickListener.onTopicReviewSummaryClicked(skillSummary)
  }
}
