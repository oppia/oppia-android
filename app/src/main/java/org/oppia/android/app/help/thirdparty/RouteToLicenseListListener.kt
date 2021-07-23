package org.oppia.android.app.help.thirdparty

/** Listener for when a selection should result to [LicenseListActivity]. */
interface RouteToLicenseListListener {
  /** Creates [Intent] to start [LicenseListActivity]. */
  fun onRouteToLicenseList(name: String, version: String, index: Int)
}
