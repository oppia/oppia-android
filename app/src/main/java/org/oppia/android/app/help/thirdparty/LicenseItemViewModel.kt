package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * Content view model for the recycler view in [LicenseListFragment]. It contains the license
 * name of the dependency which is displayed in the recyclerview.
 */
class LicenseItemViewModel(
  private val activity: AppCompatActivity,
  val licenseName: String,
  val licenseIndex: Int,
  val dependencyIndex: Int
) : ObservableViewModel() {

  /** Starts [LicenseTextViewerActivity] upon clicking on an item of the license list. */
  fun clickOnLicenseItem() {
    val routeToLicenseTextListener = activity as RouteToLicenseTextListener
    routeToLicenseTextListener.onRouteToLicenseText(dependencyIndex, licenseIndex)
  }
}
