package org.oppia.app.onBoarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dummy_on_board.*
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnboardingingFlowController
import javax.inject.Inject

/** The presenter for [OnBoardingingActivity]. */
@ActivityScope
class OnBoardingingActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val onBoardingFlowController: OnboardingingFlowController
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.dummy_on_board)
    activity.onBoard.setOnClickListener({ subscribeOnBoarding() })
  }

  private fun subscribeOnBoarding() {
    onBoardingFlowController.markOnboardingingFlowCompleted()
    val intent = Intent(activity, ProfileActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }
}
