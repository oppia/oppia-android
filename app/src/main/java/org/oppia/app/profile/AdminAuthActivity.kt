package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_ADMIN_AUTH_ADMIN_PIN = "ADMIN_AUTH_ADMIN_PIN"
const val KEY_ADMIN_AUTH_COLOR_RGB = "ADMIN_AUTH_COLOR_RGB"
const val KEY_ADMIN_AUTH_ENUM = "ADMIN_AUTH_ENUM"
const val KEY_ADMIN_AUTH_PROFILE_ID = "ADMIN_AUTH_PROFILE_ID"

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableAppCompatActivity() {
  @Inject lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    fun createAdminAuthActivityIntent(
      context: Context, adminPin: String, profileId: Int, colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      val intent = Intent(context, AdminAuthActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      intent.putExtra(KEY_ADMIN_AUTH_ADMIN_PIN, adminPin)
      intent.putExtra(KEY_ADMIN_AUTH_PROFILE_ID, profileId)
      intent.putExtra(KEY_ADMIN_AUTH_COLOR_RGB, colorRgb)
      intent.putExtra(KEY_ADMIN_AUTH_ENUM, adminPinEnum)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_ADMIN_AUTH_ENUM
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    adminAuthFragmentPresenter.handleOnCreate()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    adminAuthFragmentPresenter.handleOnSavedInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    adminAuthFragmentPresenter.handleOnRestoreInstanceState(savedInstanceState!!)
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
