package org.oppia.app.walkthrough

/** Listener for when an activity should change pages. */
interface WalkthroughActivityListener {

  /**
   * Navigates user to the previous page in a [WalkthroughActivity].
   *
   * @param currentProgress Integer describing the current page a user is on.
   */
  fun moveToPreviousPage(currentProgress: Int)
}
