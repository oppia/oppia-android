package org.oppia.android.app.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject
import org.oppia.android.app.model.OppiaLanguage

/** ViewModel for managing language selection in [OnboardingFragment]. */
class OnboardingAppLanguageViewModel @Inject constructor() : ObservableViewModel() {
  /** The selected app language displayed in the language dropdown. */
  val languageSelectionLiveData: LiveData<OppiaLanguage> get() = _languageSelectionLiveData
  private val _languageSelectionLiveData = MutableLiveData<OppiaLanguage>()

  /** Get the list of app supported languages to be displayed in the language dropdown. */
  val supportedAppLanguagesList: LiveData<List<String>> get() = _supportedAppLanguagesList
  private val _supportedAppLanguagesList = MutableLiveData<List<String>>()

  /** Sets the app language selection. */
  fun setSystemLanguageLivedata(language: OppiaLanguage) {
    _languageSelectionLiveData.value = language
  }

  /** Sets the list of app supported languages to be displayed in the language dropdown. */
  fun setSupportedAppLanguages(languageList: List<String>) {
    _supportedAppLanguagesList.value = languageList
  }
}
