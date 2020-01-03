package org.oppia.app.help

import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.OnboardingingFlow
import org.oppia.app.onBoarding.OnBoardingingActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnboardingingFlowController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** The presenter for [OnBoardingingActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  onBoardingFlowController: OnboardingingFlowController
) {

  val sample = onBoardingFlowController.getOnboardingingFlow()

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    subscribeToOnboardingingFlow()
  }

  private fun showOnboardingingActivity() {
    val intent = Intent(activity, OnBoardingingActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }

  private fun subscribeToOnboardingingFlow() {
    getOnboardingingFlow().observe(activity, Observer<OnboardingingFlow> { result ->
      if (result.alreadyOnBoardedApp) {
        val intent = Intent(activity, ProfileActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
      } else {
        showOnboardingingActivity()
      }
    })
  }

  private fun getOnboardingingFlow(): LiveData<OnboardingingFlow> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(sample, ::processOnboardingingFlowResult)
  }

  private fun processOnboardingingFlowResult(onboardingResult: AsyncResult<OnboardingingFlow>): OnboardingingFlow {
    if (onboardingResult.isFailure()) {
    }
    return onboardingResult.getOrDefault(OnboardingingFlow.getDefaultInstance())
  }
}
