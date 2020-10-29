package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val ADMIN_PIN_PROFILE_ID_EXTRA_KEY = "AdminPinActivity.admin_pin_profile_id"
const val ADMIN_PIN_COLOR_RGB_EXTRA_KEY = "AdminPinActivity.admin_pin_color_rgb"
const val ADMIN_PIN_ENUM_EXTRA_KEY = "AdminPinActivity.admin_pin_enum"

/** Activity that sets the admin's PIN. */
class AdminPinActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminPinActivityPresenter: AdminPinActivityPresenter

  companion object {
    fun createAdminPinActivityIntent(
      context: Context,
      profileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      val intent = Intent(context, AdminPinActivity::class.java)
      intent.putExtra(ADMIN_PIN_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(ADMIN_PIN_COLOR_RGB_EXTRA_KEY, colorRgb)
      intent.putExtra(ADMIN_PIN_ENUM_EXTRA_KEY, adminPinEnum)
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
