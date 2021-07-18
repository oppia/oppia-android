package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import javax.inject.Inject
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** The FAQ page activity for placement of different FAQs. */
class ThirdPartyDependencyListActivity : InjectableAppCompatActivity(),
  RouteToLicenseListListener {

  @Inject
  lateinit var thirdPartyDependencyListActivityPresenter:
    ThirdPartyDependencyListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    thirdPartyDependencyListActivityPresenter.handleOnCreate()
  }

  companion object {
    fun createThirdPartyDependencyListActivityIntent(context: Context): Intent {
      return Intent(context, ThirdPartyDependencyListActivity::class.java)
    }
  }

  override fun onRouteToLicenseList(name: String, version: String) {
    startActivity(LicenseListActivity
      .createLicenseListActivityIntent(
        this,
        name,
        version
      )
    )
  }
}
