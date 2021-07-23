package org.oppia.android.app.help.thirdparty

/** Listener for when a selection should result to [LicenseTextActivity]. */
interface RouteToLicenseTextListener {
  /** Creates [Intent] to start [LicenseTextActivity]. */
  fun onRouteToLicenseText(licenseText: String)
}