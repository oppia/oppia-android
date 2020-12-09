package org.oppia.android.app.walkthrough

/** Interface to show or hide the Progress Bar and Header Text. */
interface WalkthroughVerticalScrollListener {
  /** Changes visibility of the ProgressBar */
  fun changeProgressBarVisibility(visibility: Boolean)
}
