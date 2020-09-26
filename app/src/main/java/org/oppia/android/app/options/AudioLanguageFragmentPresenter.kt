package org.oppia.android.app.options

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.databinding.AudioLanguageFragmentBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(private val fragment: Fragment) {

  private lateinit var prefSummaryValue: String
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefKey: String,
    prefValue: String
  ): View? {
    val binding = AudioLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    prefSummaryValue = prefValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey) {
      updateAudioLanguage(it)
    }
    binding.audioLanguageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }

    binding.audioLanguageToolbar?.setNavigationOnClickListener {
      val message = languageSelectionAdapter.getSelectedLanguage()
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_AUDIO_LANGUAGE, message)
      (fragment.activity as AudioLanguageActivity).setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
      (fragment.activity as AudioLanguageActivity).finish()
    }
    createAdapter()
    return binding.root
  }

  fun getLanguageSelected(): String {
    return languageSelectionAdapter.getSelectedLanguage()
  }

  private fun updateAudioLanguage(audioLanguage: String) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity ->
        parentActivity.optionActivityPresenter.updateAudioLanguage(audioLanguage)
      is AudioLanguageActivity ->
        parentActivity.audioLanguageActivityPresenter.setLanguageSelected(audioLanguage)
    }
  }

  private fun createAdapter() {
    // TODO(#669): Replace dummy list with actual language list from backend.
    val languageList = ArrayList<String>()
    languageList.add("No Audio")
    languageList.add("English")
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setLanguageList(languageList)
    languageSelectionAdapter.setDefaultLanguageSelected(prefSummaryValue = prefSummaryValue)
  }
}
