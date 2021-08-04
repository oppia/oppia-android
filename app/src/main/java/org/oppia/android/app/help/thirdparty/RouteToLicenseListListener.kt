package org.oppia.android.app.help.thirdparty

/** Listener for when a selection should result to [LicenseListActivity]. */
interface RouteToLicenseListListener {
  /**
   * Creates [Intent] to start [LicenseListActivity].
   *
   * @param dependencyIndex: index of the dependency clicked by the user in
   *   [ThirdPartyDependencyListFragment]
   */
  fun onRouteToLicenseList(dependencyIndex: Int)
}
