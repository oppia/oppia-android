package org.oppia.android.app.topic.lessons

import org.oppia.android.app.model.StorySummary

/** Interface to transfer the selected story summary to [TopicLessonsFragmentPresenter]. */
interface StorySummarySelector {
  fun selectStorySummary(storySummary: StorySummary)
}
