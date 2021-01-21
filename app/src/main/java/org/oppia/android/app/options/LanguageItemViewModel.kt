package org.oppia.android.app.options

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * Language item view model for the recycler view in [AppLanguageFragment] and
 * [AudioLanguageFragment].
 */
class LanguageItemViewModel(
  val language: String,
  selectedLanguage: LiveData<String>,
  val languageRadioButtonListener: LanguageRadioButtonListener
) : ObservableViewModel() {
  val isLanguageSelected = ObservableBoolean()
  init {
    selectedLanguage.observeForever {
      isLanguageSelected.set(it == language)
    }
  }
}
