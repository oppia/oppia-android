package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity that allows user to log in to their profile by inputting their PIN. */
class ProfileLoginActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var profileLoginActivityPresenter: ProfileLoginActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()

    profileLoginActivityPresenter.handleOnCreate(profileId)
  }

  companion object {
    /** Params key for ProfileLoginActivity. */
    const val PROFILE_LOGIN_ACTIVITY_PARAMS_KEY = "ProfileLoginActivity.params"

    /** Creates and returns an Intent to open a new [ProfileLoginActivity]. */
    fun createProfileLoginActivityIntent(
      context: Context,
      profileId: ProfileId
    ): Intent {
      return Intent(context, ProfileLoginActivity::class.java).apply {
        putProtoExtra(PROFILE_LOGIN_ACTIVITY_PARAMS_KEY, args)
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(ScreenName.PROFILE_LOGIN_ACTIVITY)
      }
    }
  }
}
