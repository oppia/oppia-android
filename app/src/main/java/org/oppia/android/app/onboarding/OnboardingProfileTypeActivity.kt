package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.ONBOARDING_PROFILE_TYPE_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for showing the profile type selection screen. */
class OnboardingProfileTypeActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var onboardingProfileTypeActivityPresenter: OnboardingProfileTypeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()

    onboardingProfileTypeActivityPresenter.handleOnCreate(profileId)
  }

  companion object {
    /** Returns a new [Intent] open a [OnboardingProfileTypeActivity] with the specified params. */
    fun createOnboardingProfileTypeActivityIntent(context: Context): Intent {
      return Intent(context, OnboardingProfileTypeActivity::class.java).apply {
        decorateWithScreenName(ONBOARDING_PROFILE_TYPE_ACTIVITY)
      }
    }
  }
}
