package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val PIN_PASSWORD_PROFILE_ID_EXTRA_KEY = "PinPasswordActivity.pin_password_profile_id"
const val PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY = "PinPasswordActivity.pin_password_admin_pin"

/** Activity that allows user to input his or her PIN. */
class PinPasswordActivity : InjectableAppCompatActivity(), ProfileRouteDialogInterface {
  @Inject
  lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    fun createPinPasswordActivityIntent(
      context: Context,
      adminPin: String,
      profileId: Int
    ): Intent {
      val intent = Intent(context, PinPasswordActivity::class.java)
      intent.putExtra(PIN_PASSWORD_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(PIN_PASSWORD_ADMIN_PIN_EXTRA_KEY, adminPin)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
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
    pinPasswordActivityPresenter.dismissAlertDialog()
  }
}
