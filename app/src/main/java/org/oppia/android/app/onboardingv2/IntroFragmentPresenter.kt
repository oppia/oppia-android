package org.oppia.android.app.onboardingv2

import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.LearnerIntroFragmentBinding

/** The presenter for [IntroFragment]. */
class IntroFragmentPresenter @Inject constructor(
  private var fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
) {
  private lateinit var binding: LearnerIntroFragmentBinding

  private val orientation = Resources.getSystem().configuration.orientation

  /** Handle creation and binding of the  OnboardingLearnerIntroFragment layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileNickname: String,
  ): View {
    binding = LearnerIntroFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    setLearnerName(profileNickname)

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingLearnerIntroFeedback.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_feedback_text,
        appLanguageResourceHandler.getStringInLocale(R.string.app_name)
      )

    return binding.root
  }

  private fun setLearnerName(profileName: String) {
    binding.onboardingLearnerIntroTitle.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_activity_text, profileName
      )
  }
}
