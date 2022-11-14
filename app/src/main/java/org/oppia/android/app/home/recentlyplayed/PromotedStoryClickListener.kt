package org.oppia.android.app.home.recentlyplayed

import org.oppia.android.app.model.PromotedStory

/** Listener interface for when promoted story is clicked in the UI. */
interface PromotedStoryClickListener {
  /** Called when the a promoted story card is clicked by the user.
   *
   * @param promotedStory object of the story selected
   * */
  fun promotedStoryClicked(promotedStory: PromotedStory)
}
