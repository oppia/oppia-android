package org.oppia.android.app.privacypolicytermsofservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.help.thirdparty.LicenseListActivity
import javax.inject.Inject

/** The Privacy Policy page activity for placement of single PRIVACY_POLICY. */
class PrivacyPolicySingleActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var privacyPolicySingleActivityPresenter: PrivacyPolicySingleActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    intent.getStringExtra(PRIVACY_POLICY_SINGLE_ACTIVITY)
    privacyPolicySingleActivityPresenter.handleOnCreate()
  }

  companion object {
    private const val PRIVACY_POLICY_SINGLE_ACTIVITY = "PrivacyPolicySingleActivity"

    /** Returns [Intent] for [PrivacyPolicySingleActivity]. */
    fun createPrivacyPolicySingleActivityIntent(context: Context): Intent {
      return Intent(context, PrivacyPolicySingleActivity::class.java)
    }
  }
}
