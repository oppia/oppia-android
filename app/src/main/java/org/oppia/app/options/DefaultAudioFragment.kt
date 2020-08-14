package org.oppia.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE = "AUDIO_LANGUAGE_PREFERENCE"
private const val KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE =
  "AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE"
private const val KEY_SELECTED_AUDIO_LANGUAGE = "SELECTED_AUDIO_LANGUAGE"

/** The fragment to change the default audio language of the app. */
class DefaultAudioFragment : InjectableFragment() {

  @Inject
  lateinit var defaultAudioFragmentPresenter: DefaultAudioFragmentPresenter

  companion object {
    fun newInstance(prefsKey: String, prefsSummaryValue: String): DefaultAudioFragment {
      val args = Bundle()
      args.putString(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE, prefsKey)
      args.putString(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE, prefsSummaryValue)
      val fragment = DefaultAudioFragment()
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
      checkNotNull(arguments) { "Expected arguments to be passed to DefaultAudioFragment" }
    val prefsKey = args.getString(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE)
    val prefsSummaryValue = if (savedInstanceState == null) {
      args.getString(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    } else {
      savedInstanceState.get(KEY_SELECTED_AUDIO_LANGUAGE) as String
    }
    return defaultAudioFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      prefsKey!!,
      prefsSummaryValue
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      KEY_SELECTED_AUDIO_LANGUAGE,
      defaultAudioFragmentPresenter.getLanguageSelected()
    )
  }
}
