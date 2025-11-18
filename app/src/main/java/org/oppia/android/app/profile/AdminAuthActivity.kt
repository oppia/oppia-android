package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.model.AdminAuthActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.ADMIN_AUTH_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableSystemLocalizedAppCompatActivity() {
  @Inject
  lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    /** Params key for AdminAuthActivity. */
    const val ADMIN_AUTH_ACTIVITY_PARAMS_KEY = "AdminAuthActivity.params"
    fun createAdminAuthActivityIntent(
      context: Context,
      adminPin: String,
      internalProfileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      val profileId = ProfileId.newBuilder().apply { this.internalId = internalProfileId }.build()
      val args = AdminAuthActivityParams.newBuilder().apply {
        this.adminPin = adminPin
        this.colorRgb = colorRgb
        this.adminPinEnum = adminPinEnum
      }.build()
      return Intent(context, AdminAuthActivity::class.java).apply {
        putProtoExtra(ADMIN_AUTH_ACTIVITY_PARAMS_KEY, args)
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
