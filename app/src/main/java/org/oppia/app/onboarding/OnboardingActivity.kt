package org.oppia.app.onboarding

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.profile.ProfileActivity
import javax.inject.Inject

/** Activity to contain the onbaording flow for learners. */
class OnboardingActivity : InjectableAppCompatActivity(), RouteToProfileListener {
  @Inject lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }

  override fun routeToProfile() {
    startActivity(ProfileActivity.createProfileActivityIntent(this))
    finish()
  }
}
