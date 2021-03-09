package org.oppia.android.app.home.recentlyplayed

import org.oppia.android.app.model.PromotedStory

/** Listener interface for when a promoted story in the [RecentlyPlayedFragment] is clicked in the UI. */
interface PromotedStoryClickListener {
  fun onPromotedStoryClicked(promotedStory: PromotedStory)
}
