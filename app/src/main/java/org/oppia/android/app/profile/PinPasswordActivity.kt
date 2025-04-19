package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.PinPasswordActivityParams
import org.oppia.android.app.model.PinPasswordActivityStateBundle
import org.oppia.android.app.model.ScreenName.PIN_PASSWORD_ACTIVITY
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows user to input his or her PIN. */
class PinPasswordActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ProfileRouteDialogInterface {
  @Inject
  lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    /** Params key for PinPasswordActivity. */
    const val PIN_PASSWORD_ACTIVITY_PARAMS_KEY = "PinPasswordActivity.params"
    const val PINPASSWORD_ACTIVITY_STATE_KEY = "PINPASSWORD_ACTIVITY_STATE_KEY"
    fun createPinPasswordActivityIntent(
      context: Context,
      adminPin: String,
      profileId: Int
    ): Intent {
      val args = PinPasswordActivityParams.newBuilder().apply {
        this.adminPin = adminPin
        this.internalProfileId = profileId
      }.build()
      return Intent(context, PinPasswordActivity::class.java).apply {
        putProtoExtra(PIN_PASSWORD_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(PIN_PASSWORD_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val savedPin = savedInstanceState?.getProto(
      PINPASSWORD_ACTIVITY_STATE_KEY,
      PinPasswordActivityStateBundle.getDefaultInstance()
    )?.inputPin ?: ""
    pinPasswordActivityPresenter.handleOnCreate(savedPin)
  }

  override fun routeToResetPinDialog() {
    pinPasswordActivityPresenter.handleRouteToResetPinDialog()
  }

  override fun routeToSuccessDialog() {
    pinPasswordActivityPresenter.handleRouteToSuccessDialog()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val args = PinPasswordActivityStateBundle.newBuilder()
      .setInputPin(pinPasswordActivityPresenter.getInputPin())
      .build()
    outState.putProto(PINPASSWORD_ACTIVITY_STATE_KEY, args)
  }

  override fun onDestroy() {
    super.onDestroy()
    pinPasswordActivityPresenter.handleOnDestroy()
  }
}
