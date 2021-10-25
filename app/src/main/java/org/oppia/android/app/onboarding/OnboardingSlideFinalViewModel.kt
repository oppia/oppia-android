package org.oppia.android.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.domain.onboarding.AppStartupStateController
import javax.inject.Inject

/** [ViewModel] for final slide in onboarding flow. */
class OnboardingSlideFinalViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val appStartupStateController: AppStartupStateController
) : OnboardingViewPagerViewModel() {

  fun clickOnGetStarted() {
    appStartupStateController.markOnboardingFlowCompleted()
    (activity as RouteToProfileListListener).routeToProfileList()
  }
}
