package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Content view model for the recycler view in [ThirdPartyDependencyListFragment]. */
class ThirdPartyDependencyItemViewModel(
  private val activity: AppCompatActivity,
  val name: String,
  val version: String
) : ObservableViewModel() {

  /** Used to control visibility of divider. */
  val showDivider = ObservableField(true)

  /** Starts [LicenseListActivity] upon clicking on an item of the third-party dependency list. */
  fun clickOnThirdPartyDependencyItem() {
    val routeToFAQSingleListener = activity as RouteToLicenseListListener
    routeToFAQSingleListener.onRouteToLicenseList(name, version)
  }
}
