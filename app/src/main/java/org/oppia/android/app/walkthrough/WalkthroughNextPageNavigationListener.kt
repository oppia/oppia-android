package org.oppia.android.app.walkthrough

/**
 * Listener for when an activity should change pages during the app walkthrough flow's welcome
 * fragment.
 */
interface WalkthroughNextPageNavigationListener {

  /** Navigates user to the next page in a walkthrough flow's welcome fragment. */
  fun changePage()
}
