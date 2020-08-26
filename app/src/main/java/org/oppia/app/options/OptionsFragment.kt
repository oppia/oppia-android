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

/** Fragment that contains an introduction to the app. */
class OptionsFragment : InjectableFragment() {
  @Inject
  lateinit var optionsFragmentPresenter: OptionsFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return optionsFragmentPresenter.handleCreateView(inflater, container)
  }

  fun updateReadingTextSize(textSize: String) {
    optionsFragmentPresenter.updateReadingTextSize(textSize)
  }

  fun updateAppLanguage(appLanguage: String) {
    optionsFragmentPresenter.updateAppLanguage(appLanguage)
  }

  fun updateAudioLanguage(audioLanguage: String) {
    optionsFragmentPresenter.updateAudioLanguage(audioLanguage)
  }
}
