package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.ADMIN_AUTH_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

const val ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY = "AdminAuthActivity.admin_auth_admin_pin"
const val ADMIN_AUTH_COLOR_RGB_EXTRA_KEY = "AdminAuthActivity.admin_auth_color_rgb"
const val ADMIN_AUTH_ENUM_EXTRA_KEY = "AdminAuthActivity.admin_auth_enum"

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    fun createAdminAuthActivityIntent(
      context: Context,
      adminPin: String,
      profileId: ProfileId,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      return Intent(context, AdminAuthActivity::class.java).apply {
        putExtra(ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY, adminPin)
        putExtra(ADMIN_AUTH_COLOR_RGB_EXTRA_KEY, colorRgb)
        putExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, adminPinEnum)
        decorateWithScreenName(ADMIN_AUTH_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    adminAuthFragmentPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
