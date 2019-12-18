package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_PIN_PASSWORD_PROFILE_ID = "PIN_PASSWORD_PROFILE_ID"
const val KEY_PIN_PASSWORD_ADMIN_PIN = "PIN_PASSWORD_ADMIN_PIN"

/** Activity that allows user to input his or her PIN. */
class PinPasswordActivity : InjectableAppCompatActivity(), ProfileRouteDialogInterface {
  @Inject lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    fun createPinPasswordActivityIntent(context: Context, adminPin: String, profileId: Int): Intent {
      val intent = Intent(context, PinPasswordActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      intent.putExtra(KEY_PIN_PASSWORD_PROFILE_ID, profileId)
      intent.putExtra(KEY_PIN_PASSWORD_ADMIN_PIN, adminPin)
      return intent
    }
  }

  @ExperimentalCoroutinesApi
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
}
