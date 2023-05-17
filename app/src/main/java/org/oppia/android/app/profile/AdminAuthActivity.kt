package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.ADMIN_AUTH_ACTIVITY
import org.oppia.android.app.profile.AdminAuthActivityPresenter.Companion.ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY
import org.oppia.android.app.profile.AdminAuthActivityPresenter.Companion.ADMIN_AUTH_COLOR_RGB_EXTRA_KEY
import org.oppia.android.app.profile.AdminAuthActivityPresenter.Companion.ADMIN_AUTH_ENUM_EXTRA_KEY
import org.oppia.android.app.profile.AdminAuthActivityPresenter.Companion.ADMIN_AUTH_PROFILE_ID_EXTRA_KEY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    /**
     * Creates an [Intent] for opening new instances of [AdminAuthActivity].
     *
     * @param context the [Context] in which the activity should be opened
     * @param adminPin the administrator's PIN that will be used to verify the admin
     * @param profileId the ID of the profile requiring admin authentication
     * @param colorRgb the 3-byte color RGB value of the profile's background avatar color
     * @param adminPinEnum the [AdminAuthEnum] ordinal corresponding to the authentication context
     * @return the new [Intent] that cna be used to open a [AdminAuthActivity]
     */
    fun createIntent(
      context: Context,
      adminPin: String,
      profileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      return Intent(context, AdminAuthActivity::class.java).apply {
        putExtra(ADMIN_AUTH_ADMIN_PIN_EXTRA_KEY, adminPin)
        putExtra(ADMIN_AUTH_PROFILE_ID_EXTRA_KEY, profileId)
        putExtra(ADMIN_AUTH_COLOR_RGB_EXTRA_KEY, colorRgb)
        putExtra(ADMIN_AUTH_ENUM_EXTRA_KEY, adminPinEnum)
        decorateWithScreenName(ADMIN_AUTH_ACTIVITY)
      }
    }

    fun getIntentKey(): String {
      return ADMIN_AUTH_ENUM_EXTRA_KEY
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    adminAuthFragmentPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  /** Dagger injector for [AdminAuthActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: AdminAuthActivity)
  }
}
