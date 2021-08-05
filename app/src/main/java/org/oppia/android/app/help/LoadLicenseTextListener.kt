package org.oppia.android.app.help

/** Listener for when a selection should result to [LicenseTextViewerFragment] in tablet devices. */
interface LoadLicenseTextListener {
  /**
   * Starts [LicenseTextViewerFragment] in tablet devices.
   *
   * @param dependencyIndex: index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   * @param licenseIndex: index of the license clicked by the user in the [LicenseListFragment]
   */
  fun loadLicenseTextViewerFragment(dependencyIndex: Int, licenseIndex: Int)
}