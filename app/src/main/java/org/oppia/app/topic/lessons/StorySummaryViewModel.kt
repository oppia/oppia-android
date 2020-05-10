package org.oppia.app.topic.lessons

import androidx.lifecycle.ViewModel
import org.oppia.app.model.StorySummary

/** [ViewModel] for displaying a story summary. */
class StorySummaryViewModel(
  val storySummary: StorySummary,
  private val storySummarySelector: StorySummarySelector
) : TopicLessonsItemViewModel() {

  fun clickOnStorySummaryTitle() {
    storySummarySelector.selectStorySummary(storySummary)
  }
}
