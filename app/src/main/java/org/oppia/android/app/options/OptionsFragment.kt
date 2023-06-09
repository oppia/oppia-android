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
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

/** OnActivity result key to access [ReadingTextSize] result. */
const val MESSAGE_READING_TEXT_SIZE_RESULTS_KEY = "OptionsFragment.message_reading_text_size"
/** OnActivity result key to access [OppiaLanguage] result. */
const val MESSAGE_APP_LANGUAGE_ARGUMENT_KEY = "OptionsFragment.message_app_language"
/** OnActivity result key to access [AudioLanguage] result. */
const val MESSAGE_AUDIO_LANGUAGE_RESULTS_KEY = "OptionsFragment.message_audio_language"
/** Request code for [ReadingTextSize]. */
const val REQUEST_CODE_TEXT_SIZE = 1
/** Request code for [AudioLanguage]. */
const val REQUEST_CODE_AUDIO_LANGUAGE = 3

private const val IS_MULTIPANE_EXTRA = "IS_MULTIPANE_EXTRA"
private const val IS_FIRST_OPEN_EXTRA = "IS_FIRST_OPEN_EXTRA"
private const val SELECTED_FRAGMENT_EXTRA = "SELECTED_FRAGMENT_EXTRA"

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
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_EXTRA, isMultipane)
      args.putBoolean(IS_FIRST_OPEN_EXTRA, isFirstOpen)
      args.putString(SELECTED_FRAGMENT_EXTRA, selectedFragment)
      val fragment = OptionsFragment()
      fragment.arguments = args
      return fragment
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
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to OptionsFragment" }
    val isMultipane = args.getBoolean(IS_MULTIPANE_EXTRA)
    val isFirstOpen = args.getBoolean(IS_FIRST_OPEN_EXTRA)
    val selectedFragment = checkNotNull(args.getStringFromBundle(SELECTED_FRAGMENT_EXTRA))
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
