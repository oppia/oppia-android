package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity that will show list of licenses corresponding to a third-party dependency. */
class LicenseListActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var licenseListActivityPresenter: LicenseListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    licenseListActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns [Intent] for [LicenseListActivity]. */
    fun createLicenseListActivityIntent(
      context: Context,
      name: String,
      version: String
    ): Intent {
      return Intent(context, LicenseListActivity::class.java)
    }
  }
}
