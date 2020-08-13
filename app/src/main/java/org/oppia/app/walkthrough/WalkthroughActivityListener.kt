package org.oppia.app.walkthrough

/** Listener for when an activity should change a fragment position. */
interface WalkthroughActivityListener {

  fun previousPage(currentProgress: Int)
}
