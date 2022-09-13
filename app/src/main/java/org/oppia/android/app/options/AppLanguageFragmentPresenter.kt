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
  private val appLanguageSelectionViewModel: AppLanguageSelectionViewModel
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
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<AppLanguageItemViewModel>()
      .setLifecycleOwner(fragment)
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = AppLanguageItemBinding::inflate,
        setViewModel = AppLanguageItemBinding::setViewModel
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
    appLanguageSelectionViewModel.selectedLanguage.value = selectedLanguage
    updateAppLanguage(selectedLanguage)
  }
}
