package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.AudioLanguageActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.LearnerIntroFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** The presenter for [IntroFragment]. */
class IntroFragmentPresenter @Inject constructor(
  private var fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController,
  private val analyticsController: AnalyticsController,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var binding: LearnerIntroFragmentBinding

  /** Handle creation and binding of the  OnboardingLearnerIntroFragment layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileNickname: String,
    profileId: ProfileId,
    parentScreen: IntroActivityParams.ParentScreen
  ): View {
    binding = LearnerIntroFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.lifecycleOwner = fragment

    setLearnerName(profileNickname)

    markProfileOnboardingStarted(profileId)

    if (parentScreen == IntroActivityParams.ParentScreen.PROFILE_CHOOSER_SCREEN) {
      binding.onboardingStepsCount?.visibility = View.GONE
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingLearnerIntroFeedback.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_feedback_text,
        appLanguageResourceHandler.getStringInLocale(R.string.app_name)
      )

    binding.onboardingNavigationContinue.setOnClickListener {
      val intent = AudioLanguageActivity.createAudioLanguageActivityIntent(
        fragment.requireContext(),
        AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      )
      intent.decorateWithUserProfileId(profileId)
      fragment.startActivity(intent)
    }

    return binding.root
  }

  private fun setLearnerName(profileName: String) {
    binding.onboardingLearnerIntroTitle.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_activity_text, profileName
      )
  }

  private fun markProfileOnboardingStarted(profileId: ProfileId) {
    profileManagementController.markProfileOnboardingStarted(profileId)

    analyticsController.logLowPriorityEvent(
      oppiaLogger.createProfileOnboardingStartedContext(profileId),
      profileId = profileId
    )
  }
}
