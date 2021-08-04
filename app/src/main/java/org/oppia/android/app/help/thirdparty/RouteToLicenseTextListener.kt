package org.oppia.android.app.help.thirdparty

/** Listener for when a selection should result to [LicenseTextActivity]. */
interface RouteToLicenseTextListener {
  /**
   * Creates [Intent] to start [LicenseTextActivity].
   *
   * @param dependencyIndex: index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   * @param licenseIndex: index of the license clicked by the user in the [LicenseListFragment]
   */
  fun onRouteToLicenseText(dependencyIndex: Int, licenseIndex: Int)
}
