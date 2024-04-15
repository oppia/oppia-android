package org.oppia.android.app.onboardingv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity for showing the learner welcome screen. */
class IntroActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var onboardingLearnerIntroActivityPresenter: IntroActivityPresenter

  private lateinit var profileNickname: String
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val params = intent.extractParams()
    this.profileNickname = params.profileNickname
    this.internalProfileId = params.profileId

    onboardingLearnerIntroActivityPresenter.handleOnCreate(profileNickname, internalProfileId)
  }

  companion object {
    private const val PARAMS_KEY = "OnboardingIntroActivity.params"

    /**
     * A convenience function for creating a new [OnboardingLearnerIntroActivity] intent by prefilling
     * common params needed by the activity.
     */
    fun createIntroActivity(
      context: Context,
      profileNickname: String,
      internalProfileId: Int
    ): Intent {
      val params = IntroActivityParams.newBuilder()
        .setProfileNickname(profileNickname)
        .setProfileId(internalProfileId)
        .build()
      return createOnboardingLearnerIntroActivity(context, params)
    }

    private fun createOnboardingLearnerIntroActivity(
      context: Context,
      params: IntroActivityParams
    ): Intent {
      return Intent(context, IntroActivity::class.java).apply {
        putProtoExtra(PARAMS_KEY, params)
        decorateWithScreenName(ScreenName.INTRO_ACTIVITY)
      }
    }

    private fun Intent.extractParams() =
      getProtoExtra(PARAMS_KEY, IntroActivityParams.getDefaultInstance())
  }
}
