package org.oppia.app.onboarding

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity to contain the onbaording flow for learners. */
class OnboardingActivity : InjectableAppCompatActivity() {
  @Inject lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }
}