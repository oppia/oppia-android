package org.oppia.app.walkthrough

/** Listener for when an activity should move to a previous page during a walkthrough flow. */
interface WalkthroughActivityListener {

  fun moveToPreviousPage(currentProgress: Int)
}
