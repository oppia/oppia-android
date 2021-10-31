package org.oppia.android.app.help

import org.oppia.android.app.policies.Policies

/** Listener for when a selection should result in opening the policies on tablet. */
interface LoadPoliciesFragmentListener {
  /**  Called when the user wants to open the policies in tablet devices. */
  fun loadPoliciesFragment(policies: Policies)
}
