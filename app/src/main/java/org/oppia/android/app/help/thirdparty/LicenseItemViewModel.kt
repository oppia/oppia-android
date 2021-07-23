package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Content view model for the recycler view in [LicenseListFragment]. */
class LicenseItemViewModel(
  private val activity: AppCompatActivity,
  val licenseName: String,
  val index: Int
) : ObservableViewModel() {

  /** Used to control visibility of divider. */
  val showDivider = ObservableField(true)

  /** Starts [LicenseViewerActivity] upon clicking on an item of the license list. */
  fun clickOnLicenseItem() {
    val routeToLicenseTextListener = activity as RouteToLicenseTextListener
    routeToLicenseTextListener.onRouteToLicenseText(licenseName)
  }
}