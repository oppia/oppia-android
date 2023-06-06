package org.oppia.android.app.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * Language item view model for the recycler view in [AppLanguageFragment] and.
 *
 * @property language the app language corresponding to this language item to be displayed
 * @property currentSelectedLanguage the [LiveData] tracking the currently selected language
 * @property appLanguageRadioButtonListener the listener which will be called if this language is
 *     selected by the user
 */
class AppLanguageItemViewModel(
  val language: OppiaLanguage,
  val languageDisplayName: String,
  private val currentSelectedLanguage: LiveData<OppiaLanguage>,
  val appLanguageRadioButtonListener: AppLanguageRadioButtonListener,
) : ObservableViewModel() {
  /**
   * Indicates whether the language corresponding to this view model is _currently_ selected in the
   * radio button list.
   */
  val isLanguageSelected: LiveData<Boolean> by lazy {
    Transformations.map(currentSelectedLanguage) { it == language }
  }
}
