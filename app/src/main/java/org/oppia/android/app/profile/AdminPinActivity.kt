package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AdminPinActivityArguments
import org.oppia.android.app.model.ScreenName.ADMIN_PIN_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that sets the admin's PIN. */
class AdminPinActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var adminPinActivityPresenter: AdminPinActivityPresenter

  companion object {
    /** Arguments key for AdminPinActivity. */
    const val ADMIN_PIN_ACTIVITY_ARGUMENTS_KEY = "AdminPinActivity.arguments"
    fun createAdminPinActivityIntent(
      context: Context,
      profileId: Int,
      colorRgb: Int,
      adminPinEnum: Int
    ): Intent {
      val args = AdminPinActivityArguments.newBuilder().apply {
        this.internalProfileId = profileId
        this.colorRgb = colorRgb
        this.adminPinEnum = adminPinEnum
      }.build()
      return Intent(context, AdminPinActivity::class.java).apply {
        putProtoExtra(ADMIN_PIN_ACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(ADMIN_PIN_ACTIVITY)
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
