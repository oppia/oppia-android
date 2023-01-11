package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.OppiaLanguage
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
  private lateinit var appLanguage: OppiaLanguage
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefSummaryValue: OppiaLanguage
  ): View? {
    val binding = AppLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.appLanguage = prefSummaryValue
    binding.viewModel = appLanguageSelectionViewModel
    appLanguageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.languageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    return binding.root
  }

  fun getLanguageSelected(): OppiaLanguage {
    return appLanguageSelectionViewModel.selectedLanguage.value
      ?: OppiaLanguage.LANGUAGE_UNSPECIFIED
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<AppLanguageItemViewModel> {
    return singleTypeBuilderFactory.create<AppLanguageItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = AppLanguageItemBinding::inflate,
        setViewModel = AppLanguageItemBinding::setViewModel
      ).build()
  }

  private fun updateAppLanguage(appLanguage: OppiaLanguage) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateAppLanguage(appLanguage)
      is AppLanguageActivity -> parentActivity.appLanguageActivityPresenter.setLanguageSelected(
        appLanguage
      )
    }
  }

  fun onLanguageSelected(selectedLanguage: OppiaLanguage) {
    appLanguageSelectionViewModel.selectedLanguage.value = selectedLanguage
    updateAppLanguage(selectedLanguage)
  }
}
