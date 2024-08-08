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
  private lateinit var profileId: ProfileId

  /** Initializes and creates the views for [AppLanguageFragment]. */
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefSummaryValue: OppiaLanguage,
    profileId: Int
  ): View? {
    val binding = AppLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.appLanguage = prefSummaryValue
    this.profileId = ProfileId.newBuilder().apply { loggedInInternalProfileId = profileId }.build()
    appLanguageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.viewModel = appLanguageSelectionViewModel
    binding.lifecycleOwner = fragment

    binding.languageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    return binding.root
  }

  /** Returns the currently selected [OppiaLanguage], or unspecified if there isn't one. */
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

  /** Updates selected language when new language is selected. */
  fun onLanguageSelected(selectedLanguage: OppiaLanguage) {
    appLanguageSelectionViewModel.selectedLanguage.value = selectedLanguage
    updateAppLanguage(selectedLanguage)
    updateAppLanguageSelection(selectedLanguage)
  }

  private fun updateAppLanguageSelection(oppiaLanguage: OppiaLanguage) {
    val selection = AppLanguageSelection.newBuilder().apply {
      selectedLanguage = oppiaLanguage
    }.build()

    translationController.updateAppLanguage(profileId, selection).toLiveData().observe(fragment) {
      when (it) {
        is AsyncResult.Success -> appLanguage = oppiaLanguage
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "AppLanguageFragmentPresenter",
            "Failed to update language to: $oppiaLanguage.",
            it.error
          )
        }
        is AsyncResult.Pending -> {} // Wait for a result.
      }
    }
  }
}
