package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.PinPasswordActivityArguments
import org.oppia.android.app.model.ScreenName.PIN_PASSWORD_ACTIVITY
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
    /** Arguments key for PinPasswordActivity. */
    const val PIN_PASSWORD_ACTIVITY_ARGUMENTS_KEY = "PinPasswordActivity.arguments"
    fun createPinPasswordActivityIntent(
      context: Context,
      adminPin: String,
      profileId: Int
    ): Intent {
      val args = PinPasswordActivityArguments.newBuilder().apply {
        this.adminPin = adminPin
        this.internalProfileId = profileId
      }.build()
      return Intent(context, PinPasswordActivity::class.java).apply {
        putProtoExtra(PIN_PASSWORD_ACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(PIN_PASSWORD_ACTIVITY)
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
