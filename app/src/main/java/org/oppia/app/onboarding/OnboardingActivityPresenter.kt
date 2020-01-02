package org.oppia.app.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dummy_onboard.*
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.OnboardingFlowController
import javax.inject.Inject

/** The presenter for [OnboardingActivity]. */
@ActivityScope
class OnboardingActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val onboardingFlowController: OnboardingFlowController
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.dummy_onboard)
    activity.onboard.setOnClickListener({ subscribeToUserAppHistory() })
  }

  private fun subscribeToUserAppHistory() {
    onboardingFlowController.markOnboardingFlowCompleted()
    val intent = Intent(activity, ProfileActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
  }
}
