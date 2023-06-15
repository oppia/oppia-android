package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.PIN_PASSWORD_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

const val PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY = "PinPasswordActivity.pin_password_admin_pin"

/** Activity that allows user to input his or her PIN. */
class PinPasswordActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ProfileRouteDialogInterface {
  @Inject
  lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    fun createPinPasswordActivityIntent(
      context: Context,
      adminPin: String,
      profileId: ProfileId
    ): Intent {
      return Intent(context, PinPasswordActivity::class.java).apply {
        putExtra(PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY, adminPin)
        decorateWithScreenName(PIN_PASSWORD_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    pinPasswordActivityPresenter.handleOnCreate()
  }

  override fun routeToResetPinDialog() {
    pinPasswordActivityPresenter.handleRouteToResetPinDialog()
  }

  override fun routeToSuccessDialog() {
    pinPasswordActivityPresenter.handleRouteToSuccessDialog()
  }

  override fun onDestroy() {
    super.onDestroy()
    pinPasswordActivityPresenter.handleOnDestroy()
  }
}
