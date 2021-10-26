package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.privacypolicytermsofservice.PrivacyPolicyActivity
import org.oppia.android.app.privacypolicytermsofservice.TermsOfServiceActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import javax.inject.Inject

/** Activity that contains the onboarding flow for learners. */
class OnboardingActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToPrivacyPolicyListener,
  RouteToTermsOfServiceListener {
  @Inject
  lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  companion object {
    fun createOnboardingActivity(context: Context): Intent {
      return Intent(context, OnboardingActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }

  override fun routeToProfileList() {
    startActivity(ProfileChooserActivity.createProfileChooserActivity(this))
    finish()
  }

  override fun onRouteToPrivacyPolicy() {
    startActivity(
      PrivacyPolicyActivity.createPrivacyPolicyActivityIntent(
        this
      )
    )
  }

  override fun onRouteToTermsOfService() {
    startActivity(
      TermsOfServiceActivity.createTermsOfServiceActivityIntent(
        this
      )
    )
  }
}
