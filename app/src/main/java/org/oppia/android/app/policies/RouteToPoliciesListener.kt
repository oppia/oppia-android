package org.oppia.android.app.policies

/** Listener for when a selection should result in opening the policies. */
interface RouteToPoliciesListener {
  /**  Called when the user wants to open the policies page. */
  fun onRouteToPolicies(policies: Policies)
}
