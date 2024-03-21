package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.OnboardingIntroActivityParams
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity for showing the learner welcome screen. */
class OnboardingLearnerIntroActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var onboardingLearnerIntroActivityPresenter: OnboardingLearnerIntroActivityPresenter

  private lateinit var profileNickname: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val params = intent.extractParams()
    this.profileNickname = params.profileNickname

    onboardingLearnerIntroActivityPresenter.handleOnCreate(profileNickname)
  }

  companion object {
    private const val PARAMS_KEY = "OnboardingIntroActivity.params"

    /**
     * A convenience function for creating a new [OnboardingLearnerIntroActivity] intent by prefilling
     * common params needed by the activity.
     */
    fun createOnboardingLearnerIntroActivity(context: Context, profileNickname: String): Intent {
      val params = OnboardingIntroActivityParams.newBuilder()
        .setProfileNickname(profileNickname)
        .build()
      return createOnboardingLearnerIntroActivity(context, params)
    }

    private fun createOnboardingLearnerIntroActivity(
      context: Context,
      params: OnboardingIntroActivityParams
    ): Intent {
      return Intent(context, OnboardingLearnerIntroActivity::class.java).apply {
        putProtoExtra(PARAMS_KEY, params)
        decorateWithScreenName(ScreenName.ONBOARDING_LEARNER_INTRO_ACTIVITY)
      }
    }

    private fun Intent.extractParams() =
      getProtoExtra(PARAMS_KEY, OnboardingIntroActivityParams.getDefaultInstance())
  }
}
