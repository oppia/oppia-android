package org.oppia.app.topic.lessons

import org.oppia.app.model.StorySummary

/** Interface to transfer the selected story summary to [TopicLessonsFragmentPresenter]. */
interface StorySummarySelector {
  fun selectStorySummary(storySummary: StorySummary)
}
