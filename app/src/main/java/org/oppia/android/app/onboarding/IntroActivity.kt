package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.ScreenName.INTRO_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for showing the learner welcome screen. */
class IntroActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var onboardingLearnerIntroActivityPresenter: IntroActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileNickname =
      intent.getProtoExtra(PARAMS_KEY, IntroActivityParams.getDefaultInstance()).profileNickname

    val profileId = intent.extractCurrentUserProfileId()

    onboardingLearnerIntroActivityPresenter.handleOnCreate(profileNickname, profileId)
  }

  companion object {
    /** Argument key for [IntroActivity]'s intent parameters. */
    const val PARAMS_KEY = "OnboardingIntroActivity.params"

    /**
     * A convenience function for creating a new [OnboardingLearnerIntroActivity] intent by prefilling
     * common params needed by the activity.
     */
    fun createIntroActivity(context: Context): Intent {
      return Intent(context, IntroActivity::class.java).apply {
        decorateWithScreenName(INTRO_ACTIVITY)
      }
    }
  }
}
