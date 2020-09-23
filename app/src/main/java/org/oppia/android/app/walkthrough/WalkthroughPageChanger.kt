package org.oppia.app.walkthrough

/**
 * Listener for when an activity should change pages during the app walkthrough flow's welcome
 * fragment.
 */
interface WalkthroughPageChanger {

  /** Navigates user to the next page in a walkthrough flow's welcome fragment. */
  fun changePage()
}
