package org.oppia.android.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [OnboardingActivity]. */
@ActivityScope
class OnboardingActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.onboarding_activity)
    if (getOnboardingFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.onboarding_fragment_placeholder,
        OnboardingFragment()
      ).commitNow()
    }
  }

  private fun getOnboardingFragment(): OnboardingFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.onboarding_fragment_placeholder
      ) as OnboardingFragment?
  }
}
