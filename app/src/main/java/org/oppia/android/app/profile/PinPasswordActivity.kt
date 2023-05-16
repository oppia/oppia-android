package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.PIN_PASSWORD_ACTIVITY
import org.oppia.android.app.profile.PinPasswordActivityPresenter.Companion.PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY
import org.oppia.android.app.profile.PinPasswordActivityPresenter.Companion.PIN_PASSWORD_PROFILE_ID_EXTRA_KEY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows user to input his or her PIN. */
class PinPasswordActivity : InjectableAppCompatActivity(), ProfileRouteDialogInterface {
  @Inject
  lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    fun createIntent(
      context: Context,
      adminPin: String,
      profileId: Int
    ): Intent {
      return Intent(context, PinPasswordActivity::class.java).apply {
        putExtra(PIN_PASSWORD_PROFILE_ID_EXTRA_KEY, profileId)
        putExtra(PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY, adminPin)
        decorateWithScreenName(PIN_PASSWORD_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
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

  interface Injector {
    fun inject(activity: PinPasswordActivity)
  }
}
