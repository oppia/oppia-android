package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.PIN_SETUP_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Params key for PinSetupActivity. */
const val PIN_SETUP_PARAMS_KEY = "PinSetupActivityParams.params"

/** Activity for displaying the PIN setup screen. */
class PinSetupActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var pinSetupActivityPresenter: PinSetupActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    (activityComponent as ActivityComponentImpl).inject(this)
    super.onCreate(savedInstanceState)

    val profileId = intent.extractCurrentUserProfileId()
    pinSetupActivityPresenter.handleOnCreate(profileId)
  }

  companion object {
    /** Returns a new [Intent] to open a [PinSetupActivity] with the specified params. */
    fun createPinSetupActivityIntent(context: Context, profileId: ProfileId): Intent {
      return Intent(context, PinSetupActivity::class.java).apply {
        decorateWithScreenName(PIN_SETUP_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }
}
