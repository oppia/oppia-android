package org.oppia.android.app.help

/**
 * Listener for when a selection should result to [ThirdPartyDependencyListFragment] in tablet
 * devices.
 */
interface LoadThirdPartyDependencyListFragmentListener {
  /**  Called when the user wants to open the list of third-party dependencies in tablet devices. */
  fun loadThirdPartyDependencyListFragment()
}
