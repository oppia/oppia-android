package org.oppia.android.app.help.thirdparty

import android.annotation.SuppressLint
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

  @SuppressLint("ResourceType")
  private fun getRecyclerViewItemList(): ArrayList<LicenseItemViewModel> {
    val licenses = activity.resources.obtainTypedArray(R.array.third_party_dependency_license_names_array)
    val stringArrayResId = licenses.getResourceId(1, 0)
    val licenseNames = activity.resources.getStringArray(stringArrayResId)

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
    licenses.recycle()
    return arrayList
  }
}