package org.oppia.android.app.help.thirdparty

/** Listener for when a selection should result to [LicenseTextActivity]. */
interface RouteToLicenseTextListener {
  /**
   * Called when the user wants to see the license text for a particular copyright license of a
   * third-party maven dependency.
   *
   * @param dependencyIndex index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   * @param licenseIndex index of the license clicked by the user in the [LicenseListFragment]
   */
  fun onRouteToLicenseText(dependencyIndex: Int, licenseIndex: Int)
}
