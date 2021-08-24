package org.oppia.android.app.help

/** Listener for when a selection should result to [LicenseListFragment] in tablet devices. */
interface LoadLicenseListFragmentListener {
  /**
   * Called when the user wants to open the license list for a particular dependency on a tablet
   * device.
   *
   * @param dependencyIndex index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   */
  fun loadLicenseListFragment(dependencyIndex: Int)
}
