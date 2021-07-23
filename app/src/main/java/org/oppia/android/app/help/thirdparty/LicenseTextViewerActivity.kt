package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity that will show the license text of a copyright license. */
class LicenseTextViewerActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var licenseTextViewerActivityPresenter: LicenseTextViewerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    licenseTextViewerActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns [Intent] for [LicenseTextViewerActivity]. */
    fun createLicenseTextViewerActivityIntent(
      context: Context,
      text: String
    ): Intent {
      return Intent(context, LicenseTextViewerActivity::class.java)
    }
  }
}
