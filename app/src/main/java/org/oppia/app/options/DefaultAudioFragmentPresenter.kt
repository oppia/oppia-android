package org.oppia.app.options

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
    createAdapter()
    return binding.root
  }

  fun getLanguageSelected(): String {
    return languageSelectionAdapter.getSelectedLanguage()
  }

  fun updateAudioLanguage(audioLanguage: String) {
    (fragment.activity as OptionsActivity)
      .optionActivityPresenter.updateAudioLanguage(audioLanguage)
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
