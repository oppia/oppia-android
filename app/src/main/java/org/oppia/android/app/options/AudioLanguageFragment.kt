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
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The fragment to change the default audio language of the app. */
class AudioLanguageFragment : InjectableFragment(), AudioLanguageRadioButtonListener {
  @Inject lateinit var audioLanguageFragmentPresenter: AudioLanguageFragmentPresenter

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
      ) { "Expected arguments to be passed to AudioLanguageFragment" }
    return audioLanguageFragmentPresenter.handleOnCreateView(inflater, container, audioLanguage)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val state = AudioLanguageFragmentStateBundle.newBuilder().apply {
      audioLanguage = audioLanguageFragmentPresenter.getLanguageSelected()
    }.build()
    outState.putProto(FRAGMENT_SAVED_STATE_KEY, state)
  }

  override fun onLanguageSelected(audioLanguage: AudioLanguage) {
    audioLanguageFragmentPresenter.onLanguageSelected(audioLanguage)
  }

  companion object {
    private const val FRAGMENT_ARGUMENTS_KEY = "AudioLanguageFragment.arguments"
    private const val FRAGMENT_SAVED_STATE_KEY = "AudioLanguageFragment.saved_state"

    /**
     * Returns a new [AudioLanguageFragment] corresponding to the specified [AudioLanguage] (as the
     * initial selection).
     */
    fun newInstance(audioLanguage: AudioLanguage): AudioLanguageFragment {
      return AudioLanguageFragment().apply {
        arguments = Bundle().apply {
          val args = AudioLanguageFragmentArguments.newBuilder().apply {
            this.audioLanguage = audioLanguage
          }.build()
          putProto(FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }

    private fun Bundle.retrieveLanguageFromArguments(): AudioLanguage {
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
