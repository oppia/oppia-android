package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * Content view model for the recycler view in [ThirdPartyDependencyListFragment]. It contains
 * the name and version of the dependency which is displayed in the RecyclerView.
 */
class ThirdPartyDependencyItemViewModel(
  private val activity: AppCompatActivity,
  val dependencyName: String,
  val dependencyVersion: String,
  val dependencyIndex: Int
) : ObservableViewModel() {

  /** Starts [LicenseListActivity] upon clicking on an item of the third-party dependency list. */
  fun clickOnThirdPartyDependencyItem() {
    val routeToLicenseListListener = activity as RouteToLicenseListListener
    routeToLicenseListListener.onRouteToLicenseList(dependencyIndex)
  }
}
