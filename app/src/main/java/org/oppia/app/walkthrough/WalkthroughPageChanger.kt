package org.oppia.app.walkthrough

/**
 * Listener for when an activity should change pages during the app walkthrough flow.
 *
 * Functions defined here should be used in [walkthrough_welcome_fragment.xml]
 */
interface WalkthroughPageChanger {

  /** Triggers a page change in WalkthroughWelcomeFragment */
  fun changePage()
}
