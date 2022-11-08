package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.domain.help.HelpController
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Content view model for the in [LicenseTextViewerFragment] that contains the license text. */
class LicenseTextViewModel(
  val activity: AppCompatActivity,
  val dependencyIndex: Int,
  val licenseIndex: Int,
  private val helpController: HelpController,
  resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  private val dependenciesWithLicenseTexts = activity.resources.obtainTypedArray(
    R.array.third_party_dependency_license_texts_array
  )
  private val licenseTextsArrayId = dependenciesWithLicenseTexts.getResourceId(
    dependencyIndex,
    /* defValue= */ 0
  )
  private val licenseTextsArray: List<String> =
    resourceHandler.getStringArrayInLocale(licenseTextsArrayId)
  /** Text of the license to be displayed in [LicenseTextViewerFragment]. */
  val licenseText = licenseTextsArray[licenseIndex]
  fun testfunction(){
    helpController.getLargeProtoAssetFileData()
  }
}
