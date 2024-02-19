package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.options.AudioLanguageActivity
import org.oppia.android.app.options.LoadAudioLanguageListListener
import org.oppia.android.app.options.REQUEST_CODE_AUDIO_LANGUAGE
import org.oppia.android.app.options.RouteToAudioLanguageListListener
import javax.inject.Inject

/** Fragment that contains the new learner onboarding introduction of the app. */
class OnboardingLearnerIntroFragment :
  InjectableFragment(),
  RouteToAudioLanguageListListener,
  LoadAudioLanguageListListener {
  @Inject
  lateinit var onboardingLearnerIntroFragmentPresenter: OnboardingLearnerIntroFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return onboardingLearnerIntroFragmentPresenter.handleCreateView(
      inflater,
      container,
      /* fragment = */this,
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE
    )
  }

  override fun routeAudioLanguageList(audioLanguage: AudioLanguage) {
    startActivityForResult(
      AudioLanguageActivity.createAudioLanguageActivityIntent(requireContext(), audioLanguage),
      REQUEST_CODE_AUDIO_LANGUAGE
    )
  }

  override fun loadAudioLanguageFragment(audioLanguage: AudioLanguage) {}
}
