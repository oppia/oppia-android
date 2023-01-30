package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AppLanguageFragmentBinding
import org.oppia.android.databinding.AppLanguageItemBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [AppLanguageFragment]. */
class AppLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageSelectionViewModel: AppLanguageSelectionViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory,
  private val translationController: TranslationController,
  private val oppiaLogger: OppiaLogger
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
    appLanguageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.viewModel = appLanguageSelectionViewModel
    binding.lifecycleOwner = fragment

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
    updateAppLanguageSelection(selectedLanguage)
  }

  private fun updateAppLanguageSelection(oppiaLanguage: OppiaLanguage) {
    val appLanguageSelection = AppLanguageSelection.newBuilder().apply {
      selectedLanguage = oppiaLanguage
      selectedLanguageValue = oppiaLanguage.number
    }.build()

    val profileId = ProfileId.newBuilder().setInternalId(0).build()
    translationController.updateAppLanguage(
      profileId,
      appLanguageSelection
    ).toLiveData().observe(
      fragment,
      {
        when (it) {
          is AsyncResult.Success -> {
            appLanguage = oppiaLanguage
          }
          is AsyncResult.Failure ->
            oppiaLogger.e("APP_LANGUAGE_TAG", it.error.toString())
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
    )
  }
}
