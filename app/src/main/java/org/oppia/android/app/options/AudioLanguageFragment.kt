package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageFragmentArguments
import org.oppia.android.app.model.AudioLanguageFragmentStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.onboarding.AudioLanguageFragmentPresenter
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The fragment to change the default audio language of the app. */
class AudioLanguageFragment : InjectableFragment(), AudioLanguageRadioButtonListener {
  @Inject lateinit var audioLanguageFragmentPresenterV1: AudioLanguageFragmentPresenterV1

  @Inject lateinit var audioLanguageFragmentPresenter: AudioLanguageFragmentPresenter

  @Inject
  @field:EnableOnboardingFlowV2
  lateinit var enableOnboardingFlowV2: PlatformParameterValue<Boolean>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val audioLanguage =
      checkNotNull(
        savedInstanceState?.retrieveLanguageFromSavedState()
          ?: arguments?.retrieveLanguageFromArguments()
      ) { "Expected arguments to be passed to AudioLanguageFragment." }

    return if (enableOnboardingFlowV2.value) {
      val profileId = checkNotNull(arguments?.extractCurrentUserProfileId()) {
        "Expected a profileId argument to be passed to AudioLanguageFragment."
      }
      audioLanguageFragmentPresenter.handleCreateView(
        inflater,
        container,
        profileId,
        savedInstanceState
      )
    } else {
      audioLanguageFragmentPresenterV1.handleOnCreateView(inflater, container, audioLanguage)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (enableOnboardingFlowV2.value) {
      audioLanguageFragmentPresenter.handleSavedState(outState)
    } else {
      val state = AudioLanguageFragmentStateBundle.newBuilder().apply {
        audioLanguage = audioLanguageFragmentPresenterV1.getLanguageSelected()
      }.build()
      outState.putProto(FRAGMENT_SAVED_STATE_KEY, state)
    }
  }

  override fun onLanguageSelected(audioLanguage: AudioLanguage) {
    if (!enableOnboardingFlowV2.value) {
      audioLanguageFragmentPresenterV1.onLanguageSelected(audioLanguage)
    }
  }

  companion object {
    private const val FRAGMENT_ARGUMENTS_KEY = "AudioLanguageFragment.arguments"

    /** Argument key for the [AudioLanguageFragment] saved instance state bundle. */
    const val FRAGMENT_SAVED_STATE_KEY = "AudioLanguageFragment.saved_state"

    /**
     * Returns a new [AudioLanguageFragment] corresponding to the specified [AudioLanguage] (as the
     * initial selection).
     */
    fun newInstance(audioLanguage: AudioLanguage, profileId: ProfileId): AudioLanguageFragment {
      return AudioLanguageFragment().apply {
        arguments = Bundle().apply {
          val args = AudioLanguageFragmentArguments.newBuilder().apply {
            this.audioLanguage = audioLanguage
          }.build()
          putProto(FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }

    /** Returns the [AudioLanguage] stored in the fragment's arguments. */
    fun Bundle.retrieveLanguageFromArguments(): AudioLanguage {
      return getProto(
        FRAGMENT_ARGUMENTS_KEY, AudioLanguageFragmentArguments.getDefaultInstance()
      ).audioLanguage
    }

    private fun Bundle.retrieveLanguageFromSavedState(): AudioLanguage {
      return getProto(
        FRAGMENT_SAVED_STATE_KEY, AudioLanguageFragmentStateBundle.getDefaultInstance()
      ).audioLanguage
    }
  }
}
