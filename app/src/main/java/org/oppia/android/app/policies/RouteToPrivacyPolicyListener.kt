package org.oppia.android.app.policies

/** Listener for when a selection should result in opening the privacy policy on tablet. */
interface RouteToPrivacyPolicyListener {
  /**  Called when the user wants to open the privacy policy page. */
  fun onRouteToPrivacyPolicy()
}
