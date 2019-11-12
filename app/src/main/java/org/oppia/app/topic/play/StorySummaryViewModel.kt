package org.oppia.app.topic.play

import android.view.View
import androidx.lifecycle.ViewModel
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.StorySummary

// TODO(#297): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class StorySummaryViewModel(
  val storySummary: StorySummary,
  private val storySummarySelector: StorySummarySelector
) : TopicPlayItemViewModel() {
  fun clickOnStorySummaryTitle(@Suppress("UNUSED_PARAMETER") v: View) {
    storySummarySelector.selectStorySummary(storySummary)
  }
}
