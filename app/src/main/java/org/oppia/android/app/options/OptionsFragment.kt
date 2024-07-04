package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OptionsFragmentArguments
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** OnActivity result key to access [ReadingTextSize] result. */
const val MESSAGE_READING_TEXT_SIZE_RESULTS_KEY = "OptionsFragment.message_reading_text_size"

/** OnActivity result key to access [AudioLanguage] result. */
const val MESSAGE_AUDIO_LANGUAGE_RESULTS_KEY = "OptionsFragment.message_audio_language"

/** Arguments key for OptionsFragment. */
const val OPTIONS_FRAGMENT_ARGUMENTS_KEY = "OptionsFragment.arguments"

/** Fragment that contains an introduction to the app. */
class OptionsFragment : InjectableFragment() {
  @Inject
  lateinit var optionsFragmentPresenter: OptionsFragmentPresenter

  companion object {
    /** Returns a [Fragment] instance to start this fragment. */
    fun newInstance(
      isMultipane: Boolean,
      isFirstOpen: Boolean,
      selectedFragment: String
    ): OptionsFragment {

      val args = OptionsFragmentArguments.newBuilder().apply {
        this.isMultipane = isMultipane
        this.isFirstOpen = isFirstOpen
        this.selectedFragment = selectedFragment
      }.build()
      return OptionsFragment().apply {
        arguments = Bundle().apply {
          putProto(OPTIONS_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val arguments =
      checkNotNull(arguments) { "Expected arguments to be passed to OptionsFragment" }
    val args = arguments.getProto(
      OPTIONS_FRAGMENT_ARGUMENTS_KEY,
      OptionsFragmentArguments.getDefaultInstance()
    )

    val isMultipane = args.isMultipane
    val isFirstOpen = args.isFirstOpen
    val selectedFragment = checkNotNull(args.selectedFragment)
    return optionsFragmentPresenter.handleCreateView(
      inflater,
      container,
      isMultipane,
      isFirstOpen,
      selectedFragment
    )
  }

  /** Updates [ReadingTextSize] value in [OptionsFragment] when user selects new value. */
  fun updateReadingTextSize(textSize: ReadingTextSize) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.updateReadingTextSize(textSize)
    }
  }

  /** Updates [OppiaLanguage] value in [OptionsFragment] when user selects new value. */
  fun updateAppLanguage(oppiaLanguage: OppiaLanguage) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.updateAppLanguage(oppiaLanguage)
    }
  }

  /** Updates [AudioLanguage] value in [OptionsFragment] when user selects new value. */
  fun updateAudioLanguage(audioLanguage: AudioLanguage) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.updateAudioLanguage(audioLanguage)
    }
  }

  /**
   * Used to fix the race condition that happens when the presenter tries to call a function before
   * [handleCreateView] is completely executed.
   *
   * @param selectedLanguage tag for the fragment to be set as currently selected
   */
  fun setSelectedFragment(selectedLanguage: String) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.setSelectedFragment(selectedLanguage)
    }
  }
}
