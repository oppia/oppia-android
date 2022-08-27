package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.LICENSE_TEXT_VIEWER_ACTIVITY
import javax.inject.Inject
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName

/** The activity that will show the license text of a copyright license. */
class LicenseTextViewerActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var licenseTextViewerActivityPresenter: LicenseTextViewerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val dependencyIndex = intent.getIntExtra(LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX, 0)
    val licenseIndex = intent.getIntExtra(LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX, 0)
    licenseTextViewerActivityPresenter.handleOnCreate(dependencyIndex, licenseIndex)
  }

  companion object {
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX =
      "LicenseTextViewerActivity.dependency_index"
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX =
      "LicenseTextViewerActivity.license_index"

    /** Returns [Intent] for [LicenseTextViewerActivity]. */
    fun createLicenseTextViewerActivityIntent(
      context: Context,
      dependencyIndex: Int,
      licenseIndex: Int
    ): Intent {
      val intent = Intent(context, LicenseTextViewerActivity::class.java)
      intent.putExtra(LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX, dependencyIndex)
      intent.putExtra(LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX, licenseIndex)
      intent.decorateWithScreenName(LICENSE_TEXT_VIEWER_ACTIVITY)
      return intent
    }
  }
}
