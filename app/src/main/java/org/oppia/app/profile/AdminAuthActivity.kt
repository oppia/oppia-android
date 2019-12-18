package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_ADMIN_AUTH_ADMIN_PIN = "ADMIN_AUTH_ADMIN_PIN"
const val KEY_ADMIN_AUTH_COLOR_RGB = "ADMIN_AUTH_COLOR_RGB"

/** Activity that authenticates by checking for admin's PIN. */
class AdminAuthActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var adminAuthFragmentPresenter: AdminAuthActivityPresenter

  companion object {
    fun createAdminAuthActivityIntent(context: Context, adminPin: String, colorRgb: Int): Intent {
      val intent = Intent(context, AdminAuthActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      intent.putExtra(KEY_ADMIN_AUTH_ADMIN_PIN, adminPin)
      intent.putExtra(KEY_ADMIN_AUTH_COLOR_RGB, colorRgb)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    adminAuthFragmentPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
