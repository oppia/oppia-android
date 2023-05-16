package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.LicenseListActivityParams
import org.oppia.android.app.model.ScreenName.THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity for displaying a list of third-party dependencies used to build Oppia Android. */
class ThirdPartyDependencyListActivity :
  InjectableAppCompatActivity(),
  RouteToLicenseListListener {
  @Inject lateinit var presenter: ThirdPartyDependencyListActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    presenter.handleOnCreate(false)
  }

  companion object {
    /** Returns [Intent] for starting [ThirdPartyDependencyListActivity]. */
    fun createIntent(context: Context): Intent {
      return Intent(context, ThirdPartyDependencyListActivity::class.java).apply {
        decorateWithScreenName(THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY)
      }
    }
  }

  override fun onRouteToLicenseList(dependencyIndex: Int) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        licenseListActivityParams = LicenseListActivityParams.newBuilder().apply {
          this.dependencyIndex = dependencyIndex
        }.build()
      }.build()
    )
  }

  interface Injector {
    fun inject(activity: ThirdPartyDependencyListActivity)
  }
}
