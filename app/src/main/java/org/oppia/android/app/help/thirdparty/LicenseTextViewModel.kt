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
  private val dependenciesWithLicenseTexts = activity.resources.obtainTypedArray(
    R.array.third_party_dependency_license_texts_array
  )
  private val licenseTextsArrayId = dependenciesWithLicenseTexts.getResourceId(
    dependencyIndex,
    0
  )
  private val licenseTextsArray: Array<String> = activity.resources.getStringArray(
    licenseTextsArrayId
  )
  /** Text of the license to be displayed in [LicenseTextViewerFragment]. */
  val licenseText: String = Html.fromHtml(licenseTextsArray[licenseIndex]).toString()
}
