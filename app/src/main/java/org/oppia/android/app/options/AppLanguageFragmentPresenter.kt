package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AppLanguageFragmentBinding
import org.oppia.android.databinding.LanguageItemsBinding
import javax.inject.Inject

/** The presenter for [AppLanguageFragment]. */
class AppLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val languageSelectionViewModel: LanguageSelectionViewModel
) {
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
    binding.viewModel = languageSelectionViewModel
    languageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.languageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    return binding.root
  }

  fun getLanguageSelected(): String? {
    return languageSelectionViewModel.selectedLanguage.value
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<LanguageItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .Factory(fragment).create<LanguageItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = LanguageItemsBinding::inflate,
        setViewModel = LanguageItemsBinding::setViewModel
      ).build()
  }

  private fun updateAppLanguage(appLanguage: String) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateAppLanguage(appLanguage)
      is AppLanguageActivity -> parentActivity.appLanguageActivityPresenter.setLanguageSelected(
        appLanguage
      )
    }
  }

  fun onLanguageSelected(selectedLanguage: String) {
    languageSelectionViewModel.selectedLanguage.value = selectedLanguage
    updateAppLanguage(selectedLanguage)
  }
}
