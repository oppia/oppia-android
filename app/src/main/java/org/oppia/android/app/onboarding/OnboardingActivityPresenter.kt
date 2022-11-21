package org.oppia.android.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
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

    if (getSpotlightFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.onboarding_spotlight_fragment_placeholder,
        SpotlightFragment.newInstance(internalProfileId = 0),
        SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
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

  private fun getSpotlightFragment(): SpotlightFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.onboarding_spotlight_fragment_placeholder
      ) as? SpotlightFragment
  }
}
