package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The FAQ page activity for placement of different FAQs. */
class LicenseListActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var licenseListActivityPresenter:
    LicenseListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    licenseListActivityPresenter.handleOnCreate()
  }

  companion object {
    fun createLicenseListActivityIntent(
      context: Context,
      name: String,
      version: String
    ): Intent {
      return Intent(context, LicenseListActivity::class.java)
    }
  }
}
