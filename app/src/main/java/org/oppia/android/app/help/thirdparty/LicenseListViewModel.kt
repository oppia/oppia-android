package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.help.HelpViewModel
import javax.inject.Inject

/**
 * View model in [LicenseListFragment] that contains the list of licenses corresponding to a
 * third-party dependency.
 */
class LicenseListViewModel @Inject constructor(
  val activity: AppCompatActivity,
  val dependencyIndex: Int
) : HelpViewModel() {

  /** Stores the list of licenses of the third-party dependency. */
  val licenseItemList: List<LicenseItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): List<LicenseItemViewModel> {
    val thirdPartyDependencyLicenseNamesArray = activity.resources.obtainTypedArray(
      R.array.third_party_dependency_license_names_array
    )
    val licenseNamesArrayId = thirdPartyDependencyLicenseNamesArray.getResourceId(
      dependencyIndex,
      /* defValue= */ 0
    )
    val licenseNamesArray = activity.resources.getStringArray(licenseNamesArrayId)
    val itemList = licenseNamesArray.mapIndexed { licenseIndex, name ->
      LicenseItemViewModel(activity, name, licenseIndex, dependencyIndex, isMultipane.get()!!)
    }
    thirdPartyDependencyLicenseNamesArray.recycle()
    return itemList
  }
}
