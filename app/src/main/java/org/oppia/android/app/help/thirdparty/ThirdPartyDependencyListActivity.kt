package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for displaying a list of third-party dependencies used to build Oppia Android. */
class ThirdPartyDependencyListActivity :
  InjectableAppCompatActivity(),
  RouteToLicenseListListener {

  @Inject
  lateinit var thirdPartyDependencyListActivityPresenter:
    ThirdPartyDependencyListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    thirdPartyDependencyListActivityPresenter.handleOnCreate(false)
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
          dependencyIndex = dependencyIndex
        )
    )
  }
}
