package org.oppia.android.app.options

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController
import javax.inject.Inject

/** Language list view model for the recycler view in [AppLanguageFragment]. */
@FragmentScope
class AppLanguageSelectionViewModel @Inject constructor(
  val fragment: Fragment,
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  /** The name of the app language currently selected in the radio button list. */
  val selectedLanguage = MutableLiveData<OppiaLanguage>()
  private val appLanguageRadioButtonListener = fragment as AppLanguageRadioButtonListener

  /** The list of [AppLanguageItemViewModel]s which can be bound to a recycler view. */
  val recyclerViewAppLanguageList: List<AppLanguageItemViewModel> by lazy {
    createItemViewModel(
      translationController
        .getAllAppLanguageDefinitions()
    )
  }

  private fun createItemViewModel(languageList: List<OppiaLanguage>):
    List<AppLanguageItemViewModel> {
      val appLanguageItemViewModelList = arrayListOf<AppLanguageItemViewModel>()

      for (OppiaLanguage in languageList.filter { it !in IGNORED_APP_LANGUAGES }) {
        appLanguageItemViewModelList.add(
          AppLanguageItemViewModel(
            OppiaLanguage,
            appLanguageResourceHandler.computeLocalizedDisplayName(OppiaLanguage),
            selectedLanguage,
            appLanguageRadioButtonListener
          )
        )
      }
      return appLanguageItemViewModelList
    }

  private companion object {
    private val IGNORED_APP_LANGUAGES =
      listOf(
        OppiaLanguage.LANGUAGE_UNSPECIFIED, OppiaLanguage.UNRECOGNIZED
      )
  }
}
