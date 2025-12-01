package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.VisibleForTesting
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
    private const val EXTRA_LOGIN_FLOW = "ProfileLoginActivity.login_flow"

    enum class LoginFlow(val value: Int) {
      OPEN_EXISTING_PROFILE(0),
      ADD_NEW_LEARNER(1);

      companion object {
        fun fromValue(value: Int): LoginFlow =
          values().firstOrNull { it.value == value } ?: OPEN_EXISTING_PROFILE
      }
    }

    /** Creates and returns an Intent to open a new [ProfileLoginActivity]. */
    fun createProfileLoginActivityIntent(
      context: Context,
      profileId: ProfileId,
      loginFlow: LoginFlow = LoginFlow.OPEN_EXISTING_PROFILE
    ): Intent {
      return Intent(context, ProfileLoginActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(ScreenName.PROFILE_LOGIN_ACTIVITY)
        putExtra(EXTRA_LOGIN_FLOW, loginFlow.value)
      }
    }

    /** Convenience intent for launching login as part of the add-profile flow. */
    fun createProfileLoginForAddProfileIntent(
      context: Context,
      profileId: ProfileId
    ): Intent = createProfileLoginActivityIntent(context, profileId, LoginFlow.ADD_NEW_LEARNER)

    @VisibleForTesting
    fun extractLoginFlowFromIntent(intent: Intent): LoginFlow =
      LoginFlow.fromValue(intent.getIntExtra(EXTRA_LOGIN_FLOW, LoginFlow.OPEN_EXISTING_PROFILE.value))
  }

  override fun routeToResetPinDialog(profileId: ProfileId, profileName: String) {
    profileLoginActivityPresenter.handleRouteToResetPinDialog(profileId, profileName)
  }

  override fun routeToSuccessDialog() {
    profileLoginActivityPresenter.handleRouteToSuccessDialog()
  }
}
