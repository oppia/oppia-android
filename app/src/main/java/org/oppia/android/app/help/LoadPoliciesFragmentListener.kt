package org.oppia.android.app.help

import org.oppia.android.app.model.PolicyPage

/**
 * Listener for when a selection should result in displaying a policy page (e.g. the Privacy Policy)
 * on tablet.
 */
interface LoadPoliciesFragmentListener {
  /**  Called when the user wants to view an app policy. */
  fun loadPoliciesFragment(policyPage: PolicyPage)
}
