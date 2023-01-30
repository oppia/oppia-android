package org.oppia.android.app.options

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.options.AppLanguageSelectionViewModel.Companion.IGNORED_APP_LANGUAGES
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
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

  private val appLanguageResultLiveData: LiveData<AsyncResult<List<OppiaLanguage>>> by lazy {
    translationController.getSupportedAppLanguages().toLiveData()
  }

  private val appLanguageListLiveData: LiveData<List<OppiaLanguage>> by lazy {
    getAppLanguageData()
  }

  private fun getAppLanguageData(): LiveData<List<OppiaLanguage>> {
    return Transformations.map(appLanguageResultLiveData, ::processAppLanguageResult)
  }

  private fun processAppLanguageResult(
    asyncResultAppLanguageListData: AsyncResult<List<OppiaLanguage>>
  ): List<OppiaLanguage> {
    return when (asyncResultAppLanguageListData) {
      is AsyncResult.Success -> {
        asyncResultAppLanguageListData.value
      }
      else -> {
        emptyList()
      }
    }
  }

  /** The list of [AppLanguageItemViewModel]s which can be bound to a recycler view. */
  val recyclerViewAppLanguageList: LiveData<List<AppLanguageItemViewModel>> by lazy {
    Transformations.map(appLanguageListLiveData, ::processAppLanguageList)
  }

  private fun processAppLanguageList(appLanguageList: List<OppiaLanguage>):
    List<AppLanguageItemViewModel> {
      return appLanguageList.filter { it !in IGNORED_APP_LANGUAGES }.map {
        AppLanguageItemViewModel(
          it,
          appLanguageResourceHandler.computeLocalizedDisplayName(it),
          selectedLanguage,
          appLanguageRadioButtonListener
        )
      }
    }

  private companion object {
    private val IGNORED_APP_LANGUAGES =
      listOf(
        OppiaLanguage.LANGUAGE_UNSPECIFIED, OppiaLanguage.UNRECOGNIZED
      )
  }
}
