package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE = "AUDIO_LANGUAGE_PREFERENCE"
private const val KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE =
  "AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE"
private const val SELECTED_AUDIO_LANGUAGE_SAVED_KEY =
  "AudioLanguageFragment.selected_audio_language"

/** The fragment to change the default audio language of the app. */
class AudioLanguageFragment : InjectableFragment() {

  @Inject
  lateinit var audioLanguageFragmentPresenter: AudioLanguageFragmentPresenter

  companion object {
    fun newInstance(prefsKey: String, prefsSummaryValue: String): AudioLanguageFragment {
      val args = Bundle()
      args.putString(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE, prefsKey)
      args.putString(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE, prefsSummaryValue)
      val fragment = AudioLanguageFragment()
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
      checkNotNull(arguments) { "Expected arguments to be passed to AudioLanguageFragment" }
    val prefsKey = args.getString(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE)
    val audioLanguageDefaultSummary = checkNotNull(
      args.getString(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    )
    val prefsSummaryValue = if (savedInstanceState == null) {
      audioLanguageDefaultSummary
    } else {
      savedInstanceState.get(SELECTED_AUDIO_LANGUAGE_SAVED_KEY) as? String
        ?: audioLanguageDefaultSummary
    }
    return audioLanguageFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      prefsKey!!,
      prefsSummaryValue
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      SELECTED_AUDIO_LANGUAGE_SAVED_KEY,
      audioLanguageFragmentPresenter.getLanguageSelected()
    )
  }
}
