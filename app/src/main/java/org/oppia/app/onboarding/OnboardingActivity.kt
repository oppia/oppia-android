package org.oppia.app.onboarding

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.help.OnboardingActivityPresenter
import org.oppia.app.profile.ProfileActivity
import javax.inject.Inject

/** An activity that shows a temporary Onboarding page until the app is onborarded then navigates to [ProfileActivity]. */
class OnboardingActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }
}

