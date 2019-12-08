package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_ADMIN_PIN_PROFILE_ID = "ADMIN_PIN_PROFILE_ID"

/** Activity that sets the admin's PIN. */
class AdminPinActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminPinActivityPresenter: AdminPinActivityPresenter

  companion object {
    fun createAdminPinActivity(context: Context, profileId: Int, color: Int): Intent {
      val intent = Intent(context, AdminPinActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      intent.putExtra(KEY_ADMIN_PIN_PROFILE_ID, profileId)
      intent.putExtra(KEY_PROFILE_AVATAR_COLOR, color)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    adminPinActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
