package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model in [LicenseListFragment]. */
class LicenseListViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {
  private val arrayList = ArrayList<LicenseItemViewModel>()

  /** Stores the list of third-party dependencies. */
  val licenseItemList: List<LicenseItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  private fun getRecyclerViewItemList(): ArrayList<LicenseItemViewModel> {
    val licenseNames: Array<String> =
      activity.resources.getStringArray(R.array.license_names_array)

    licenseNames.forEachIndexed { index, name ->
      val licenseItemViewModel =
        LicenseItemViewModel(activity, name, index)
      if (index == licenseNames.lastIndex) {
        licenseItemViewModel.showDivider.set(false)
      } else {
        licenseItemViewModel.showDivider.set(true)
      }
      arrayList.add(licenseItemViewModel)
    }
    return arrayList
  }
}