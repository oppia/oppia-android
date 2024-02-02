package org.oppia.android.app.onboarding.onboardingv2

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.OnboardingLearnerIntroActivityBinding
import javax.inject.Inject

private const val TAG_LEARNER_INTRO_FRAGMENT = "TAG_LEARNER_INTRO_FRAGMENT"

/** The Presenter for [OnboardingLearnerIntroActivity]. */
@ActivityScope
class OnboardingLearnerIntroActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: OnboardingLearnerIntroActivityBinding

  /** Handle creation and binding of the  OnboardingProfileTypeActivity layout. */
  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView(activity, R.layout.onboarding_learner_intro_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    if (getOnboardingLearnerIntroFragment() == null) {
      val onboardingLearnerIntroFragment = OnboardingLearnerIntroFragment()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.learner_intro_fragment_placeholder,
        onboardingLearnerIntroFragment,
        TAG_LEARNER_INTRO_FRAGMENT
      )
        .commitNow()
    }
  }

  private fun getOnboardingLearnerIntroFragment(): OnboardingLearnerIntroFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_LEARNER_INTRO_FRAGMENT
    ) as? OnboardingLearnerIntroFragment
  }
}
