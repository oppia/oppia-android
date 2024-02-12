package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment
import org.oppia.android.databinding.OnboardingLearnerIntroFragmentBinding
import javax.inject.Inject
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.options.AudioLanguageActivity
import org.oppia.android.app.options.LoadAudioLanguageListListener
import org.oppia.android.app.options.OptionsAudioLanguageViewModel
import org.oppia.android.app.options.REQUEST_CODE_AUDIO_LANGUAGE
import org.oppia.android.app.options.RouteToAudioLanguageListListener

/** The presenter for [OnboardingLearnerIntroFragment]. */
class OnboardingLearnerIntroFragmentPresenter @Inject constructor(
  private var fragment: Fragment,
  private val activity: AppCompatActivity
) {
  private lateinit var binding: OnboardingLearnerIntroFragmentBinding
  private lateinit var routeToAudioLanguageListListener: RouteToAudioLanguageListListener
  private lateinit var loadAudioLanguageListListener: LoadAudioLanguageListListener

  /** Handle creation and binding of the  OnboardingLearnerIntroFragment layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    fragment: OnboardingLearnerIntroFragment,
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
    binding.let {
      it.lifecycleOwner = fragment
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingNavigationContinue.setOnClickListener {
      routeToAudioLanguageList(audioLanguage)
    }

    return binding.root
  }

  private fun routeToAudioLanguageList(audioLanguage: AudioLanguage) {
    routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage)
  }
}
