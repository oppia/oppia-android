package org.oppia.android.app.help

/** Listener for when a selection should result to [LicenseTextViewerFragment] in tablet devices. */
interface LoadLicenseTextViewerFragmentListener {
  /**
   * Called when the user wants to see the license text for a particular copyright license of a
   * third-party Maven dependency in a tablet device.
   *
   * @param dependencyIndex index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   * @param licenseIndex index of the license clicked by the user in the [LicenseListFragment]
   */
  fun loadLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int)
}
