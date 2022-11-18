package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY

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
      val spotlightFragment = SpotlightFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, -1)
      spotlightFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.onboarding_spotlight_fragment_placeholder,
        spotlightFragment, SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
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
      ) as SpotlightFragment?
  }
}
