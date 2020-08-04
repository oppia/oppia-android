package org.oppia.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.AppLanguageFragmentBinding
import javax.inject.Inject

/** The presenter for [AppLanguageFragment]. */
class AppLanguageFragmentPresenter @Inject constructor(private val fragment: Fragment) {
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  private lateinit var prefSummaryValue: String

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefKey: String,
    prefSummaryValue: String
  ): View? {
    val binding = AppLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.prefSummaryValue = prefSummaryValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey) {
      updateAppLanguage(it)
    }
    binding.languageRecyclerView.apply {
      adapter = languageSelectionAdapter
    }

    createAdapter()
    return binding.root
  }

  fun updateAppLanguage(appLanguage: String) {
    (fragment.activity as OptionsActivity).optionActivityPresenter.updateAppLanguage(appLanguage)
  }

  fun getLanguageSelected(): String {
    return languageSelectionAdapter.getSelectedLanguage()
  }

  private fun createAdapter() {
    // TODO(#669): Replace dummy list with actual language list from backend.
    val languageList = ArrayList<String>()
    languageList.add("English")
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setLanguageList(languageList)
    languageSelectionAdapter.setDefaultLanguageSelected(prefSummaryValue = prefSummaryValue)
  }
}
