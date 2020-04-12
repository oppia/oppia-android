package org.oppia.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.databinding.AppLanguageFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

@FragmentScope
class AppLanguageFragmentPresenter @Inject constructor() {
  private lateinit var binding: AppLanguageFragmentBinding
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  private lateinit var prefSummaryValue: String
  private lateinit var fragment: AppLanguageFragment

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefKey: String,
    prefSummaryValue: String
  ): View? {
    binding = AppLanguageFragmentBinding.inflate(inflater, container, false)
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

  private fun updateAppLanguage(appLanguage: String) {
    (fragment.activity as OptionsActivity).optionActivityPresenter.updateAppLanguage(appLanguage)
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

  fun setFragment(fragment: AppLanguageFragment) {
    this.fragment = fragment
  }
}