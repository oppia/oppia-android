package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model in [LicenseListFragment]. */
class LicenseListViewModel @Inject constructor(
  val activity: AppCompatActivity,
  val dependencyIndex: Int
) : ObservableViewModel() {
  private val arrayList = ArrayList<LicenseItemViewModel>()

  /** Stores the list of licenses of the third-party dependency. */
  val licenseItemList: List<LicenseItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<LicenseItemViewModel> {
    val thirdPartyDependencyLicenseNamesArray = activity.resources.obtainTypedArray(
      R.array.third_party_dependency_license_names_array
    )
    val licenseNamesArrayId = thirdPartyDependencyLicenseNamesArray.getResourceId(
      dependencyIndex,
      /* defValue= */ 0
    )
    val licenseNamesArray = activity.resources.getStringArray(licenseNamesArrayId)

    licenseNamesArray.forEachIndexed { licenseIndex, name ->
      val licenseItemViewModel =
        LicenseItemViewModel(activity, name, licenseIndex, dependencyIndex)
      arrayList.add(licenseItemViewModel)
    }
    thirdPartyDependencyLicenseNamesArray.recycle()
    return arrayList
  }
}
