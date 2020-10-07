package org.oppia.android.app.walkthrough

/** Listener for when an activity should hide and show ProgressBar And HeaderTextView. */
interface WalkThroughVerticalScrollListener {
  /** Hides ProgressBar and show HeaderTextView*/
  fun hideProgressBarAndShowHeader()

  /** Hides HeaderTextView and show ProgressBar*/
  fun hideHeaderAndShowProgressBar()
}
