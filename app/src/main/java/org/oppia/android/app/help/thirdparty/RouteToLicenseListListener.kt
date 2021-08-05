package org.oppia.android.app.help.thirdparty

/** Listener for when a selection should result to [LicenseListActivity]. */
interface RouteToLicenseListListener {
  /**
   * Called when the user wants to open the license list for a particular dependency.
   *
   * @param dependencyIndex index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   */
  fun onRouteToLicenseList(dependencyIndex: Int)
}
