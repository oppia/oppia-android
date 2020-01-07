package org.oppia.app.onBoarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.on_boarding_activity.*
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnBoardingingFlowController
import javax.inject.Inject

/** The presenter for [OnBoardingingActivity]. */
@ActivityScope
class OnBoardingingActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val onBoardingFlowController: OnBoardingingFlowController
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.on_boarding_activity)
    activity.onBoard.setOnClickListener({ subscribeOnBoarding() })
  }

  private fun subscribeOnBoarding() {
    onBoardingFlowController.markOnBoardingingFlowCompleted()
    val intent = Intent(activity, ProfileActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }
}
