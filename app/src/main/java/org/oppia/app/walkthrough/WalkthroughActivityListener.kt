package org.oppia.app.walkthrough

/** Listener for when an activity should change pages. */
interface WalkthroughActivityListener {

  fun previousPage(currentProgress: Int)
}
