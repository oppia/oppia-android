package org.oppia.android.app.home.recentlyplayed

import org.oppia.android.app.model.PromotedStory

/** Listener interface for when ongoing story is clicked in the UI. */
interface PromotedStoryClickListener {
  fun onOngoingStoryClicked(promotedStory: PromotedStory)
}
