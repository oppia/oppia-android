package org.oppia.app.home.continueplaying

import android.view.View
import androidx.lifecycle.ViewModel
import org.oppia.app.model.PromotedStory

// TODO(#297): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class OngoingStoryViewModel(
  val ongoingStory: PromotedStory,
  private val ongoingStoryClickListener: OngoingStoryClickListener
) : ContinuePlayingViewModel() {

  val whiteBackground = 0xffffff

  fun clickOnOngoingStoryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    ongoingStoryClickListener.onOngoingStoryClicked(ongoingStory)
  }
}
