package org.oppia.app.topic.play

import org.oppia.app.model.StorySummary

/** Interface to transfer the selected story summary to [TopicPlayFragmentPresenter]. */
interface StorySummarySelector {
  fun selectedStorySummary(storySummary: StorySummary)
}
