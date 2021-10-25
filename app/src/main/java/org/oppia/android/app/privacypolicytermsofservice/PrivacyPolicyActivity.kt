package org.oppia.android.app.privacypolicytermsofservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The Privacy Policy page activity for placement of single PRIVACY_POLICY. */
class PrivacyPolicyActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var privacyPolicyActivityPresenter: PrivacyPolicyActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    intent.getStringExtra(PRIVACY_POLICY_ACTIVITY)
    privacyPolicyActivityPresenter.handleOnCreate()
  }

  companion object {
    private const val PRIVACY_POLICY_ACTIVITY = "PrivacyPolicyActivity"

    /** Returns [Intent] for [PrivacyPolicyActivity]. */
    fun createPrivacyPolicyActivityIntent(context: Context): Intent {
      return Intent(context, PrivacyPolicyActivity::class.java)
    }
  }
}
