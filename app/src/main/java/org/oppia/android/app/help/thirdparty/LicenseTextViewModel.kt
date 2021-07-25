package org.oppia.android.app.help.thirdparty

import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Content view model for the recycler view in [LicenseListFragment]. */
class LicenseTextViewModel(
  val activity: AppCompatActivity,
  val dependencyIndex: Int,
  licenseIndex: Int
) : ObservableViewModel() {
  val licenses =
    activity.resources.obtainTypedArray(R.array.third_party_dependency_license_texts_array)
  val stringArrayResId = licenses.getResourceId(dependencyIndex, 0)
  private val licenseTextsArray: Array<String> = activity.resources.getStringArray(stringArrayResId)
  val licenseText: String = Html.fromHtml(licenseTextsArray[licenseIndex]).toString()
}
