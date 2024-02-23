package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.LicenseTextViewerActivityParams
import org.oppia.android.app.model.ScreenName.LICENSE_TEXT_VIEWER_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity that will show the license text of a copyright license. */
class LicenseTextViewerActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var licenseTextViewerActivityPresenter: LicenseTextViewerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      LICENSE_TEXT_VIEWER_ACTIVITY_PARAMS_KEY,
      LicenseTextViewerActivityParams.getDefaultInstance()
    )
    val dependencyIndex = args?.dependencyIndex ?: 0
    val licenseIndex = args?.licenseIndex ?: 0
    licenseTextViewerActivityPresenter.handleOnCreate(dependencyIndex, licenseIndex)
  }

  companion object {
    /** Params key for LicenseTextViewerActivity. */
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_PARAMS_KEY =
      "LicenseTextViewerActivity.params"

    /** Returns [Intent] for [LicenseTextViewerActivity]. */
    fun createLicenseTextViewerActivityIntent(
      context: Context,
      dependencyIndex: Int,
      licenseIndex: Int
    ): Intent {
      val args = LicenseTextViewerActivityParams.newBuilder().apply {
        this.dependencyIndex = dependencyIndex
        this.licenseIndex = licenseIndex
      }.build()
      return Intent(context, LicenseTextViewerActivity::class.java).apply {
        putProtoExtra(LICENSE_TEXT_VIEWER_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(LICENSE_TEXT_VIEWER_ACTIVITY)
      }
    }
  }
}
