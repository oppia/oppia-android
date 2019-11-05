package org.oppia.app.home.continueplaying

import org.oppia.app.model.PromotedStory

/** Listener interface for when story summaries are clicked in the UI. */
interface OngoingStoryClickListener {
  fun onOngoingStoryClicked(promotedStory: PromotedStory)
}
