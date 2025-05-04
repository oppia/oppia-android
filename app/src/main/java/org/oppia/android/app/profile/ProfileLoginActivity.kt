package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity that allows user to log in to their profile by inputting their PIN. */
class ProfileLoginActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ProfileRouteDialogInterface {

  @Inject
  lateinit var profileLoginActivityPresenter: ProfileLoginActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()

    profileLoginActivityPresenter.handleOnCreate(profileId)
  }

  companion object {
    /** Creates and returns an Intent to open a new [ProfileLoginActivity]. */
    fun createProfileLoginActivityIntent(
      context: Context,
      profileId: ProfileId
    ): Intent {
      return Intent(context, ProfileLoginActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(ScreenName.PROFILE_LOGIN_ACTIVITY)
      }
    }
  }

  override fun routeToResetPinDialog(profileId: ProfileId, profileName: String) {
    profileLoginActivityPresenter.handleRouteToResetPinDialog(profileId, profileName)
  }

  override fun routeToSuccessDialog() {
    profileLoginActivityPresenter.handleRouteToSuccessDialog()
  }

  override fun onDestroy() {
    super.onDestroy()
    // profileLoginActivityPresenter.handleOnDestroy()
  }
}
