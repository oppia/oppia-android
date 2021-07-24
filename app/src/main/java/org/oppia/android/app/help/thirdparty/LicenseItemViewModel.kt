package org.oppia.android.app.help.thirdparty

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Content view model for the recycler view in [LicenseListFragment]. */
class LicenseItemViewModel(
  private val activity: AppCompatActivity,
  val licenseName: String,
  val licenseItemIndex: Int,
  val dependencyIndex: Int
) : ObservableViewModel() {

  /** Used to control visibility of divider. */
  val showDivider = ObservableField(true)

  /** Starts [LicenseTextViewerActivity] upon clicking on an item of the license list. */
  fun clickOnLicenseItem() {
    val routeToLicenseTextListener = activity as RouteToLicenseTextListener
    Log.d("ViewModel", "Dependency Index : $dependencyIndex.")
    Log.d("ViewModel", "LicenseItem Index : $licenseItemIndex.")
    routeToLicenseTextListener.onRouteToLicenseText(dependencyIndex, licenseItemIndex)
  }
}