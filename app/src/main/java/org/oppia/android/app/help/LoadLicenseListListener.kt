package org.oppia.android.app.help

/** Listener for when a selection should result to [LicenseListFragment] in tablet devices. */
interface LoadLicenseListListener {
  /**
   * Starts [LicenseListFragment] in tablet devices.
   *
   * @param dependencyIndex: index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   */
  fun loadLicenseListFragment(dependencyIndex: Int)
}
