package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.privacypolicytermsofservice.PrivacyPolicySingleActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import javax.inject.Inject

/** Activity that contains the onboarding flow for learners. */
class OnboardingActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToPrivacyPolicySingleListener {
  @Inject
  lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  companion object {
    fun createOnboardingActivity(context: Context): Intent {
      return Intent(context, OnboardingActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }

  override fun routeToProfileList() {
    startActivity(ProfileChooserActivity.createProfileChooserActivity(this))
    finish()
  }

  override fun onRouteToPrivacyPolicySingle() {
    startActivity(
      PrivacyPolicySingleActivity.createPrivacyPolicySingleActivityIntent(
        this
      )
    )
  }
}
