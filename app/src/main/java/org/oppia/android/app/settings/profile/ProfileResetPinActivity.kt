package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Argument key for the ID of the profile resetting their pin. */
const val PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY =
  "ProfileResetPinActivity.profile_reset_pin_profile_id"

/**Argument key for confirming profile is admin. */
const val PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY =
  "ProfileResetPinActivity.profile_reset_pin_is_admin"

/** Activity that allows user to change a profile's PIN. */
class ProfileResetPinActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileResetPinActivityPresenter: ProfileResetPinActivityPresenter

  companion object {

    /** Returns [Intent] for opening [ProfileResetPinActivity]. */
    fun createProfileResetPinActivity(context: Context, profileId: Int, isAdmin: Boolean): Intent {
      val intent = Intent(context, ProfileResetPinActivity::class.java)
      intent.putExtra(PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY, isAdmin)
      return intent
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
