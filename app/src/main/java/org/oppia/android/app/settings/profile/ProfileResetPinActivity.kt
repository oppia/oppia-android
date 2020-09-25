package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_PROFILE_RESET_PIN_PROFILE_ID = "KEY_PROFILE_RESET_PIN_PROFILE_ID"
const val KEY_PROFILE_RESET_PIN_IS_ADMIN = "KEY_PROFILE_RESET_PIN_IS_ADMIN"

/** Activity that allows user to change a profile's PIN. */
class ProfileResetPinActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileResetPinActivityPresenter: ProfileResetPinActivityPresenter

  companion object {
    fun createProfileResetPinActivity(context: Context, profileId: Int, isAdmin: Boolean): Intent {
      val intent = Intent(context, ProfileResetPinActivity::class.java)
      intent.putExtra(KEY_PROFILE_RESET_PIN_PROFILE_ID, profileId)
      intent.putExtra(KEY_PROFILE_RESET_PIN_IS_ADMIN, isAdmin)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileResetPinActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
