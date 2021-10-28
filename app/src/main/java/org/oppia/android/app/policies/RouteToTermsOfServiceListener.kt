package org.oppia.android.app.policies

/** Listener for when a selection should result in opening the terms of service page on tablet. */
interface RouteToTermsOfServiceListener {
  /**  Called when the user wants to open the terms of service page. */
  fun onRouteToTermsOfService()
}
