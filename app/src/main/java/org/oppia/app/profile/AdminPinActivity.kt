package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_ADMIN_PIN_PROFILE_ID = "ADMIN_PIN_PROFILE_ID"
const val KEY_ADMIN_PIN_COLOR_RGB = "ADMIN_PIN_COLOR_RGB"

/** Activity that sets the admin's PIN. */
class AdminPinActivity : InjectableAppCompatActivity() {
  @Inject lateinit var adminPinActivityPresenter: AdminPinActivityPresenter
  private lateinit var sharedPreferences: SharedPreferences

  companion object {
    fun createAdminPinActivityIntent(context: Context, profileId: Int, colorRgb: Int): Intent {
      val intent = Intent(context, AdminPinActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      intent.putExtra(KEY_ADMIN_PIN_PROFILE_ID, profileId)
      intent.putExtra(KEY_ADMIN_PIN_COLOR_RGB, colorRgb)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this)
    adminPinActivityPresenter.handleOnCreate(savedInstanceState,sharedPreferences)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    adminPinActivityPresenter.handleOnRestoreInstanceState(savedInstanceState)
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
