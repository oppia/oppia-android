package org.oppia.app.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.on_boarding_activity.*
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnBoardingFlowController
import javax.inject.Inject

/** The presenter for [OnBoardingActivity]. */
@ActivityScope
class OnBoardingActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val onBoardingFlowController: OnBoardingFlowController
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.on_boarding_activity)
    activity.on_board.setOnClickListener { subscribeOnBoarding() }
  }

  private fun subscribeOnBoarding() {
    onBoardingFlowController.markOnBoardingFlowCompleted()
    val intent = Intent(activity, ProfileActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }
}
