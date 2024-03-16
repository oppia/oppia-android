package org.oppia.android.app.onboarding.onboardingv2

import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.options.LoadAudioLanguageListListener
import org.oppia.android.app.options.RouteToAudioLanguageListListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.OnboardingLearnerIntroFragmentBinding
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [OnboardingLearnerIntroFragment]. */
class OnboardingLearnerIntroFragmentPresenter @Inject constructor(
  private var fragment: Fragment,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
) {
  private lateinit var binding: OnboardingLearnerIntroFragmentBinding
  private lateinit var routeToAudioLanguageListListener: RouteToAudioLanguageListListener
  private lateinit var loadAudioLanguageListListener: LoadAudioLanguageListListener

  private val orientation = Resources.getSystem().configuration.orientation

  /** Handle creation and binding of the  OnboardingLearnerIntroFragment layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    fragment: OnboardingLearnerIntroFragment,
    profileNickname: String,
    audioLanguage: AudioLanguage
  ): View {
    this.routeToAudioLanguageListListener = fragment
    this.loadAudioLanguageListListener = fragment
    this.fragment = fragment

    binding = OnboardingLearnerIntroFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    setLearnerName(profileNickname)

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingNavigationContinue.setOnClickListener {
      routeToAudioLanguageList(audioLanguage)
    }

    binding.onboardingLearnerIntroFeedback.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_feedback_text,
        appLanguageResourceHandler.getStringInLocale(R.string.app_name)
      )

    binding.onboardingStepsCount.visibility =
      if (orientation == Configuration.ORIENTATION_PORTRAIT) View.VISIBLE else View.GONE

    return binding.root
  }

  private fun routeToAudioLanguageList(audioLanguage: AudioLanguage) {
    routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage)
  }

  private fun setLearnerName(profileName: String) {
    binding.onboardingLearnerIntroTitle.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_activity_text, profileName
      )
  }
}
