package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.LICENSE_TEXT_VIEWER_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject
import org.oppia.android.app.model.LicenseTextViewerFragmentArguments
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

/** The activity that will show the license text of a copyright license. */
class LicenseTextViewerActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var licenseTextViewerActivityPresenter: LicenseTextViewerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      LICENSETEXTVIEWERACTIVITY_ARGUMENTS_KEY,
      LicenseTextViewerFragmentArguments.getDefaultInstance()
    )
    val dependencyIndex = args?.dependencyIndex ?: 0
    val licenseIndex = args?.licenseIndex ?: 0
    licenseTextViewerActivityPresenter.handleOnCreate(dependencyIndex, licenseIndex)
  }

  companion object {
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_DEP_INDEX =
      "LicenseTextViewerActivity.dependency_index"
    private const val LICENSE_TEXT_VIEWER_ACTIVITY_LICENSE_INDEX =
      "LicenseTextViewerActivity.license_index"

    /** Argument key for LicenseTextViewerActivity. */
    private const val LICENSETEXTVIEWERACTIVITY_ARGUMENTS_KEY =
      "LicenseTextViewerActivity.Arguments"

    /** Returns [Intent] for [LicenseTextViewerActivity]. */
    fun createLicenseTextViewerActivityIntent(
      context: Context,
      dependencyIndex: Int,
      licenseIndex: Int
    ): Intent {
      return Intent(context, LicenseTextViewerActivity::class.java).apply {
        val args = LicenseTextViewerFragmentArguments.newBuilder().apply {
          this.dependencyIndex = dependencyIndex
          this.licenseIndex = licenseIndex
        }.build()
        putProtoExtra(LICENSETEXTVIEWERACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(LICENSE_TEXT_VIEWER_ACTIVITY)
      }
    }
  }
}
