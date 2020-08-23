package org.oppia.app.options

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.DefaultAudioFragmentBinding
import javax.inject.Inject

/** The presenter for [DefaultAudioFragment]. */
class DefaultAudioFragmentPresenter @Inject constructor(private val fragment: Fragment) {

  private lateinit var prefSummaryValue: String
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefKey: String,
    prefValue: String
  ): View? {
    val binding = DefaultAudioFragmentBinding.inflate(
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

    // TODO(#1200): Stop the toolbar functionality in the multipane (add non-null receiver (?)).
    binding.audioLanguageToolbar.setNavigationOnClickListener {
      val message = languageSelectionAdapter.getSelectedLanguage()
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_AUDIO_LANGUAGE, message)
      (fragment.activity as DefaultAudioActivity).setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
      (fragment.activity as DefaultAudioActivity).finish()
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
      is DefaultAudioActivity ->
        parentActivity.defaultAudioActivityPresenter.setLanguageSelected(audioLanguage)
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
