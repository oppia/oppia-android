package org.oppia.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

const val KEY_MESSAGE_READING_TEXT_SIZE = "TEXT_SIZE"
const val KEY_MESSAGE_APP_LANGUAGE = "APP_LANGUAGE"
const val KEY_MESSAGE_AUDIO_LANGUAGE = "AUDIO_LANGUAGE"
const val REQUEST_CODE_TEXT_SIZE = 1
const val REQUEST_CODE_APP_LANGUAGE = 2
const val REQUEST_CODE_AUDIO_LANGUAGE = 3

private const val IS_MULTIPANE_EXTRA = "IS_MULTIPANE_EXTRA"
private const val IS_FIRST_OPEN_EXTRA = "IS_FIRST_OPEN_EXTRA"
private const val SELECTED_FRAGMENT_EXTRA = "SELECTED_FRAGMENT_EXTRA"

/** Fragment that contains an introduction to the app. */
class OptionsFragment : InjectableFragment() {
  @Inject
  lateinit var optionsFragmentPresenter: OptionsFragmentPresenter

  companion object {
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
    fragmentComponent.inject(this)
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
    val selectedFragment = checkNotNull(args.getString(SELECTED_FRAGMENT_EXTRA))
    return optionsFragmentPresenter.handleCreateView(
      inflater,
      container,
      isMultipane,
      isFirstOpen,
      selectedFragment
    )
  }

  fun updateReadingTextSize(textSize: String) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.updateReadingTextSize(textSize)
    }
  }

  fun updateAppLanguage(appLanguage: String) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.updateAppLanguage(appLanguage)
    }
  }

  fun updateAudioLanguage(audioLanguage: String) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.updateAudioLanguage(audioLanguage)
    }
  }

  fun setSelectedFragment(selectedLanguage: String) {
    optionsFragmentPresenter.runAfterUIInitialization {
      optionsFragmentPresenter.setSelectedFragment(selectedLanguage)
    }
  }
}
