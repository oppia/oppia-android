package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AdminAuthActivityParams
import org.oppia.android.app.model.ScreenName.ADMIN_AUTH_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    /** Params key for AdminAuthActivity. */
    const val ADMIN_AUTH_ACTIVITY_PARAMS_KEY = "AdminAuthActivity.params"
    fun createAdminAuthActivityIntent(
      context: Context,
      adminPin: String,
      profileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      val args = AdminAuthActivityParams.newBuilder().apply {
        this.adminPin = adminPin
        this.internalProfileId = profileId
        this.colorRgb = colorRgb
        this.adminPinEnum = adminPinEnum
      }.build()
      return Intent(context, AdminAuthActivity::class.java).apply {
        putProtoExtra(ADMIN_AUTH_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(ADMIN_AUTH_ACTIVITY)
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
