package org.oppia.android.app.help.thirdparty

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model in [LicenseListFragment]. */
class LicenseListViewModel @Inject constructor(
  val activity: AppCompatActivity,
  val index: Int
) : ObservableViewModel() {
  private val arrayList = ArrayList<LicenseItemViewModel>()

  /** Stores the list of third-party dependencies. */
  val licenseItemList: List<LicenseItemViewModel> by lazy {
    getRecyclerViewItemList()
  }

  @SuppressLint("ResourceType")
  private fun getRecyclerViewItemList(): ArrayList<LicenseItemViewModel> {
    val licenses =
      activity.resources.obtainTypedArray(R.array.third_party_dependency_license_names_array)
    val stringArrayResId = licenses.getResourceId(index, -1)
    val licenseNames = activity.resources.getStringArray(stringArrayResId)

    licenseNames.forEachIndexed { licenseIndex, name ->
      val licenseItemViewModel =
        LicenseItemViewModel(activity, name, licenseIndex, index)
      arrayList.add(licenseItemViewModel)
    }
    licenses.recycle()
    return arrayList
  }
}
