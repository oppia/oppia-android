package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity for showing the learner welcome screen. */
class OnboardingLearnerIntroActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var onboardingLearnerIntroActivityPresenter: OnboardingLearnerIntroActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    onboardingLearnerIntroActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] open a [OnboardingLearnerIntroActivity] with the specified params. */
    fun createOnboardingLearnerIntroActivity(context: Context): Intent {
      return Intent(context, OnboardingLearnerIntroActivity::class.java).apply {
        decorateWithScreenName(ScreenName.ONBOARDING_LEARNER_INTRO_ACTIVITY)
      }
    }
  }
}
