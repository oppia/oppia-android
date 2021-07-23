package org.oppia.android.app.help.thirdparty


import javax.inject.Inject
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Content view model for the recycler view in [LicenseListFragment]. */
class LicenseTextViewModel @Inject constructor(
  val licenseText: String,
) : ObservableViewModel() {
//  val licenses = activity.resources.obtainTypedArray(R.array.third_party_dependency_license_texts_array)
//  val stringArrayResId = licenses.getResourceId(0, 0)
//  val licenseTexts = activity.resources.getStringArray(stringArrayResId)
//  val license = licenseTexts[0]
}