package org.oppia.app.splash

import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.OnBoardingFlow
import org.oppia.app.onboarding.OnBoardingActivity
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnBoardingFlowController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  onBoardingFlowController: OnBoardingFlowController
) {

  private val onBoardingFlowData = onBoardingFlowController.getOnBoardingFlow()

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    subscribeToOnBoardingFlow()
  }

  private fun showOnBoardingActivity() {
    val intent = Intent(activity, OnBoardingActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }

  private fun subscribeToOnBoardingFlow() {
    getOnBoardingFlow().observe(activity, Observer<OnBoardingFlow> { result ->
      if (result.alreadyOnBoardedApp) {
        val intent = Intent(activity, ProfileActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
      } else {
        showOnBoardingActivity()
      }
    })
  }

  private fun getOnBoardingFlow(): LiveData<OnBoardingFlow> {
    // If there's an error loading the data, assume the default.
    return Transformations.map(onBoardingFlowData, ::processOnBoardingFlowResult)
  }

  private fun processOnBoardingFlowResult(onBoardingResult: AsyncResult<OnBoardingFlow>): OnBoardingFlow {
    if (onBoardingResult.isFailure()) {
    }
    return onBoardingResult.getOrDefault(OnBoardingFlow.getDefaultInstance())
  }
}
