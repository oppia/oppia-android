package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The FAQ page activity for placement of different FAQs. */
class ThirdPartyDependencyListActivity :
  InjectableAppCompatActivity(),
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
    /** Returns [Intent] for starting [ThirdPartyDependencyListActivity]. */
    fun createThirdPartyDependencyListActivityIntent(context: Context): Intent {
      return Intent(context, ThirdPartyDependencyListActivity::class.java)
    }
  }

  override fun onRouteToLicenseList(dependencyIndex: Int) {
    startActivity(
      LicenseListActivity
        .createLicenseListActivityIntent(
          context = this,
          index = dependencyIndex
        )
    )
  }
}
