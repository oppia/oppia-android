package org.oppia.app.home.recentlyplayed

import org.oppia.app.model.PromotedStory

/** Listener interface for when ongoing story is clicked in the UI. */
interface OngoingStoryClickListener {
  fun onOngoingStoryClicked(promotedStory: PromotedStory)
}
