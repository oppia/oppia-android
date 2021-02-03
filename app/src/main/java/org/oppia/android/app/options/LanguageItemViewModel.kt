package org.oppia.android.app.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * Language item view model for the recycler view in [AppLanguageFragment] and
 * [AudioLanguageFragment].
 */
class LanguageItemViewModel(
  val language: String,
  private val selectedLanguage: LiveData<String>,
  val languageRadioButtonListener: LanguageRadioButtonListener
) : ObservableViewModel() {
  val isLanguageSelected: LiveData<Boolean> by lazy {
    Transformations.map(selectedLanguage) { it == language }
  }
}
