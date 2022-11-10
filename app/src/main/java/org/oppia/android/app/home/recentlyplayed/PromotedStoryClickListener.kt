package org.oppia.android.app.home.recentlyplayed

import org.oppia.android.app.model.PromotedStory

/** Listener interface for when promoted story is clicked in the UI. */
interface PromotedStoryClickListener {
  fun promotedStoryClicked(promotedStory: PromotedStory)
}
