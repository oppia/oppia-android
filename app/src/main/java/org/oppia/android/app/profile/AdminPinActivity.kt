package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.ADMIN_PIN_ACTIVITY
import org.oppia.android.app.profile.AdminPinActivityPresenter.Companion.ADMIN_PIN_COLOR_RGB_EXTRA_KEY
import org.oppia.android.app.profile.AdminPinActivityPresenter.Companion.ADMIN_PIN_ENUM_EXTRA_KEY
import org.oppia.android.app.profile.AdminPinActivityPresenter.Companion.ADMIN_PIN_PROFILE_ID_EXTRA_KEY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that sets the admin's PIN. */
class AdminPinActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminPinActivityPresenter: AdminPinActivityPresenter

  companion object {
    /**
     * Creates an [Intent] for opening new instances of [AdminPinActivity].
     *
     * @param context the [Context] in which the activity should be opened
     * @param profileId the ID of the admin profile whose PIN is being updated
     * @param colorRgb the 3-byte color RGB value of the profile's background avatar color
     * @param adminPinEnum the [AdminAuthEnum] ordinal indicating to where the user should be
     *     navigated after the PIN has been updated
     * @return the new [Intent] that cna be used to open a [AdminPinActivity]
     */
    fun createIntent(
      context: Context,
      profileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      return Intent(context, AdminPinActivity::class.java).apply {
        putExtra(ADMIN_PIN_PROFILE_ID_EXTRA_KEY, profileId)
        putExtra(ADMIN_PIN_COLOR_RGB_EXTRA_KEY, colorRgb)
        putExtra(ADMIN_PIN_ENUM_EXTRA_KEY, adminPinEnum)
        decorateWithScreenName(ADMIN_PIN_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    adminPinActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  /** Dagger injector for [AdminPinActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: AdminPinActivity)
  }
}
