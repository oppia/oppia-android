package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileResetPinActivityParams
import org.oppia.android.app.model.ScreenName.PROFILE_RESET_PIN_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows user to change a profile's PIN. */
class ProfileResetPinActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var profileResetPinActivityPresenter: ProfileResetPinActivityPresenter

  companion object {
    /** Params key for ProfileResetPinActivity. */
    const val PROFILE_RESET_PIN_ACTIVITY_PARAMS_KEY = "ProfileResetPinActivity.params"

    /** Returns [Intent] for opening [ProfileResetPinActivity]. */
    fun createProfileResetPinActivity(context: Context, profileId: Int, isAdmin: Boolean): Intent {

      val args = ProfileResetPinActivityParams.newBuilder().apply {
        this.internalProfileId = profileId
        this.isAdmin = isAdmin
      }.build()
      return Intent(context, ProfileResetPinActivity::class.java).apply {
        putProtoExtra(PROFILE_RESET_PIN_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(PROFILE_RESET_PIN_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileResetPinActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
