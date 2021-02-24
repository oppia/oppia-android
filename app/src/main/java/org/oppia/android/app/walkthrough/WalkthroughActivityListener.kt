package org.oppia.android.app.walkthrough

/** Listener for when an activity should move to a previous page during a walkthrough flow. */
interface WalkthroughActivityListener {

  /**
   * Navigates user to the previous page in a [WalkthroughActivity].
   *
   * @param currentProgress Integer describing the current page a user is on.
   */
  fun moveToPreviousPage(currentProgress: Int)
}
