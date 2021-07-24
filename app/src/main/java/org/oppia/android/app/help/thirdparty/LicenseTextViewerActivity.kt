package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity that will show the license text of a copyright license. */
class LicenseTextViewerActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var licenseTextViewerActivityPresenter: LicenseTextViewerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val dependencyIndex = intent.getIntExtra(LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX, 1)
    val licenseIndex = intent.getIntExtra(LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX, 1)
    licenseTextViewerActivityPresenter.handleOnCreate(dependencyIndex, licenseIndex)
  }

  companion object {
    /** Returns [Intent] for [LicenseTextViewerActivity]. */
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX = "LicenseTextViewerActivity.DepIndex"
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX =
      "LicenseTextViewerActivity.LicenseIndex"

    fun createLicenseTextViewerActivityIntent(
      context: Context,
      dependencyIndex: Int,
      licenseIndex: Int
    ): Intent {
      val intent = Intent(context, LicenseTextViewerActivity::class.java)
      intent.putExtra(LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX, dependencyIndex)
      intent.putExtra(LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX, licenseIndex)
      Log.d("In Intent", "Dependency Index : $dependencyIndex.")
      Log.d("In intent", "License Index : $licenseIndex.")
      return intent
    }
  }
}
