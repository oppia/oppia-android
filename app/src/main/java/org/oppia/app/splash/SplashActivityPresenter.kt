package org.oppia.app.splash

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.OnboardingFlow
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.onboarding.OnboardingFlowController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: Logger,
  private val onboardingFlowController: OnboardingFlowController
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    subscribeToOnboardingFlow()
  }

  private fun subscribeToOnboardingFlow() {
    getOnboardingFlow().observe(activity, Observer<OnboardingFlow> { result ->
      if (result.alreadyOnboardedApp) {
        activity.startActivity(ProfileActivity.createProfileActivity(activity))
      } else {
        activity.startActivity(OnboardingActivity.createOnboardingActivity(activity))
      }
      activity.finish()
    })
  }

  private fun getOnboardingFlow(): LiveData<OnboardingFlow> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(onboardingFlowController.getOnboardingFlow(), ::processOnboardingFlowResult)
  }

  private fun processOnboardingFlowResult(onboardingResult: AsyncResult<OnboardingFlow>): OnboardingFlow {
    if (onboardingResult.isFailure()) {
      logger.e("SplashActivity", "Failed to retrieve onboarding flow " + onboardingResult.getErrorOrNull())
    }
    return onboardingResult.getOrDefault(OnboardingFlow.getDefaultInstance())
  }
}
