package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AppLanguageFragmentBinding
import org.oppia.android.databinding.AppLanguageItemBinding
import javax.inject.Inject

/** The presenter for [AppLanguageFragment]. */
class AppLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageSelectionViewModel: AppLanguageSelectionViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private lateinit var prefSummaryValue: String
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefSummaryValue: String
  ): View? {
    val binding = AppLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.prefSummaryValue = prefSummaryValue
    binding.viewModel = appLanguageSelectionViewModel
    appLanguageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.languageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    return binding.root
  }

  fun getLanguageSelected(): String? {
    return appLanguageSelectionViewModel.selectedLanguage.value
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<AppLanguageItemViewModel> {
    return singleTypeBuilderFactory.create<AppLanguageItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = AppLanguageItemBinding::inflate,
        setViewModel = AppLanguageItemBinding::setViewModel
      ).build()
  }

  private fun updateAppLanguage(appLanguage: String) {
    (fragment.activity as AppLanguageSelectionListener).onLanguageSelected(appLanguage)
  }

  fun onLanguageSelected(selectedLanguage: String) {
    appLanguageSelectionViewModel.selectedLanguage.value = selectedLanguage
    updateAppLanguage(selectedLanguage)
  }
}
