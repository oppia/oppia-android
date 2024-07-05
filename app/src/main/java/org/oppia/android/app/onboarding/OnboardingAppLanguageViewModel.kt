package org.oppia.android.app.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class OnboardingAppLanguageViewModel @Inject constructor() :
  ObservableViewModel() {
  /** The selected app language displayed in the language dropdown. */
  val languageSelectionLiveData: LiveData<String> get() = _languageSelectionLiveData
  private val _languageSelectionLiveData = MutableLiveData<String>()

  /** Sets the app language selection. */
  fun setSelectedLanguageDisplayName(language: String) {
    println("setSelectedLanguageDisplayName Livedata $language")
    _languageSelectionLiveData.value = language
  }

  /** Get the list of app supported languages to be displayed in the language dropdown. */
  val supportedAppLanguagesList: LiveData<List<String>> get() = _supportedAppLanguagesList
  private val _supportedAppLanguagesList = MutableLiveData<List<String>>()

  /** Sets the list of app supported languages to be displayed in the language dropdown. */
  fun setSupportedAppLanguages(languageList: List<String>) {
    _supportedAppLanguagesList.value = languageList
  }
}
