package org.oppia.android.app.policies

/** Listener for when a selection should result to [TermsOfServiceActivity]. */
interface RouteToTermsOfServiceListener {
  /**  Called when the user wants to open the Terms Of Service page. */
  fun onRouteToTermsOfService()
}
