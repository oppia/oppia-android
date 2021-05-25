package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/**
 * Language list view model for the recycler view in [AppLanguageFragment] and
 * [AudioLanguageFragment].
 */
@FragmentScope
class LanguageSelectionViewModel @Inject constructor(
  val activity: AppCompatActivity,
  val fragment: Fragment
) : ObservableViewModel() {

  val selectedLanguage = MutableLiveData<String>()
  val languageRadioButtonListener = fragment as LanguageRadioButtonListener

  private val appLanguagesList = listOf<LanguageItemViewModel>(
    LanguageItemViewModel("English", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("French", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("Hindi", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("Chinese", selectedLanguage, languageRadioButtonListener)
  )
  private val audioLanguagesList = listOf<LanguageItemViewModel>(
    LanguageItemViewModel("No Audio", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("English", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("French", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("Hindi", selectedLanguage, languageRadioButtonListener),
    LanguageItemViewModel("Chinese", selectedLanguage, languageRadioButtonListener)
  )

  val recyclerViewAudioLanguageList: List<LanguageItemViewModel> by lazy {
    audioLanguagesList
  }

  val recyclerViewAppLanguageList: List<LanguageItemViewModel> by lazy {
    appLanguagesList
  }
}
