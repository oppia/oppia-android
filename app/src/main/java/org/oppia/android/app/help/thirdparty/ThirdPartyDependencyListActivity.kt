package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for displaying a list of third-party dependencies used to build Oppia Android. */
class ThirdPartyDependencyListActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToLicenseListListener {

  @Inject
  lateinit var thirdPartyDependencyListActivityPresenter:
    ThirdPartyDependencyListActivityPresenter
  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent.extractCurrentUserProfileId()
    thirdPartyDependencyListActivityPresenter.handleOnCreate(false)
  }

  companion object {
    /** Returns [Intent] for starting [ThirdPartyDependencyListActivity]. */
    fun createThirdPartyDependencyListActivityIntent(
      context: Context,
      profileId: ProfileId
    ): Intent {
      return Intent(context, ThirdPartyDependencyListActivity::class.java).apply {
        decorateWithScreenName(THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun onRouteToLicenseList(dependencyIndex: Int) {
    startActivity(
      LicenseListActivity
        .createLicenseListActivityIntent(
          context = this,
          dependencyIndex = dependencyIndex,
          profileId
        )
    )
  }
}
