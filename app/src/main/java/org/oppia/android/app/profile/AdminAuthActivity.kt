package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY = "AdminAuthActivity.admin_auth_admin_pin"
const val ADMIN_AUTH_COLOR_RGB_EXTRA_KEY = "AdminAuthActivity.admin_auth_color_rgb"
const val ADMIN_AUTH_ENUM_EXTRA_KEY = "AdminAuthActivity.admin_auth_enum"
const val ADMIN_AUTH_PROFILE_ID_EXTRA_KEY = "AdminAuthActivity.admin_auth_profile_id"

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    fun createAdminAuthActivityIntent(
      context: Context,
      adminPin: String,
      profileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      val intent = Intent(context, AdminAuthActivity::class.java)
      intent.putExtra(ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY, adminPin)
      intent.putExtra(ADMIN_AUTH_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(ADMIN_AUTH_COLOR_RGB_EXTRA_KEY, colorRgb)
      intent.putExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, adminPinEnum)
      return intent
    }

    fun getIntentKey(): String {
      return ADMIN_AUTH_ENUM_EXTRA_KEY
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
