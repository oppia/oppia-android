package org.oppia.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.onboarding.OnboardingFlowController
import javax.inject.Inject

/** [ViewModel] for final slide in onboarding flow. */
class OnboardingSlideFinalViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val onboardingFlowController: OnboardingFlowController
) : ObservableViewModel() {

  fun clickOnGetStarted() {
    onboardingFlowController.markOnboardingFlowCompleted()
    (activity as RouteToProfileListListener).routeToProfileList()
  }
}
