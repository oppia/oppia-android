package org.oppia.app.help

import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.OnBoardingingFlow
import org.oppia.app.onBoarding.OnBoardingingActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnBoardingingFlowController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  onBoardingFlowController: OnBoardingingFlowController
) {

  val onBoardingFlow = onBoardingFlowController.getOnBoardingingFlow()

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    subscribeToOnBoardingingFlow()
  }

  private fun showOnBoardingingActivity() {
    val intent = Intent(activity, OnBoardingingActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }

  private fun subscribeToOnBoardingingFlow() {
    getOnBoardingingFlow().observe(activity, Observer<OnBoardingingFlow> { result ->
      if (result.alreadyOnBoardedApp) {
        val intent = Intent(activity, ProfileActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
      } else {
        showOnBoardingingActivity()
      }
    })
  }

  private fun getOnBoardingingFlow(): LiveData<OnBoardingingFlow> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(onBoardingFlow, ::processOnBoardingingFlowResult)
  }

  private fun processOnBoardingingFlowResult(onboardingResult: AsyncResult<OnBoardingingFlow>): OnBoardingingFlow {
    if (onboardingResult.isFailure()) {
    }
    return onboardingResult.getOrDefault(OnBoardingingFlow.getDefaultInstance())
  }
}
