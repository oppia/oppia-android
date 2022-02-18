package org.oppia.android.app.policies

import org.oppia.android.app.model.PoliciesArguments.PolicyPage

/** Listener for when a selection should result in displaying a policy page (e.g. the Privacy Policy). */
interface RouteToPoliciesListener {
  /** Called when the user wants to view an app policy. */
  fun onRouteToPolicies(policyPage: PolicyPage)
}
