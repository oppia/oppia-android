package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.ADMIN_PIN_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

const val ADMIN_PIN_COLOR_RGB_EXTRA_KEY = "AdminPinActivity.admin_pin_color_rgb"
const val ADMIN_PIN_ENUM_EXTRA_KEY = "AdminPinActivity.admin_pin_enum"

/** Activity that sets the admin's PIN. */
class AdminPinActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var adminPinActivityPresenter: AdminPinActivityPresenter

  companion object {
    fun createAdminPinActivityIntent(
      context: Context,
      profileId: ProfileId,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      return Intent(context, AdminPinActivity::class.java).apply {
        putExtra(ADMIN_PIN_COLOR_RGB_EXTRA_KEY, colorRgb)
        putExtra(ADMIN_PIN_ENUM_EXTRA_KEY, adminPinEnum)
        decorateWithScreenName(ADMIN_PIN_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    adminPinActivityPresenter.handleOnCreate()

    val toolbar: Toolbar = findViewById(R.id.admin_pin_toolbar)
    setSupportActionBar(toolbar)
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
