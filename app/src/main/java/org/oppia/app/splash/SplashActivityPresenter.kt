package org.oppia.app.help

import android.content.Intent
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
import org.oppia.domain.OnboardingFlowController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** The presenter for [OnboardingActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  onboardingFlowController: OnboardingFlowController
) {

  val sample = onboardingFlowController.getOnboardingFlow()

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    subscribeToOnboardingFlow()
  }

  private fun showOnboardingActivity() {
    val intent = Intent(activity, OnboardingActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }

  private fun subscribeToOnboardingFlow() {
    getOnboardingFlow().observe(activity, Observer<OnboardingFlow> { result ->
      if (result.alreadyOnBoardedApp) {
        val intent = Intent(activity, ProfileActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
      } else {
        showOnboardingActivity()
      }
    })
  }

  private fun getOnboardingFlow(): LiveData<OnboardingFlow> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(sample, ::processOnboardingFlowResult)
  }

  private fun processOnboardingFlowResult(appHistoryResult: AsyncResult<OnboardingFlow>): OnboardingFlow {
    if (appHistoryResult.isFailure()) {
    }
    return appHistoryResult.getOrDefault(OnboardingFlow.getDefaultInstance())
  }
}
