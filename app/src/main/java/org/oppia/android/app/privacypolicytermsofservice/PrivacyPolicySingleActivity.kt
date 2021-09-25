package org.oppia.android.app.privacypolicytermsofservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The Privacy Policy page activity for placement of single PRIVACY_POLICY. */
class PrivacyPolicySingleActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var privacyPolicySingleActivityPresenter: PrivacyPolicySingleActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val privacyPolicy = intent.getStringExtra(PRIVACY_POLICY_SINGLE_ACTIVITY)
    privacyPolicySingleActivityPresenter.handleOnCreate()
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val PRIVACY_POLICY_SINGLE_ACTIVITY = "PrivacyPolicySingleActivity"

    fun createPrivacyPolicySingleActivityIntent(context: Context): Intent {
      val intent = Intent(context, PrivacyPolicySingleActivity::class.java)
      return intent
    }
  }
}
