package org.oppia.android.app.policies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for displaying the app's privacy policy. */
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

    /** Returns the [Intent] for opening [PrivacyPolicyActivity]. */
    fun createPrivacyPolicyActivityIntent(context: Context): Intent {
      return Intent(context, PrivacyPolicyActivity::class.java)
    }
  }
}
