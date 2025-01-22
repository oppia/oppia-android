package org.oppia.android.app.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * Language item view model for the recycler view in [AppLanguageFragment] and
 * [AudioLanguageFragment].
 *
 * @property language the [AudioLanguage] corresponding to this language item to be displayed
 * @property languageDisplayName the human-readable version of [language] to display to users to
 *     represent the language
 * @property currentSelectedLanguage the [LiveData] tracking the currently selected [AudioLanguage]
 * @property audioLanguageRadioButtonListener the listener which will be called if this language is
 *     selected by the user
 */
class AudioLanguageItemViewModel(
  val language: AudioLanguage,
  val languageDisplayName: String,
  private val currentSelectedLanguage: LiveData<AudioLanguage>,
  val audioLanguageRadioButtonListener: AudioLanguageRadioButtonListener,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  /**
   * Indicates whether the language corresponding to this view model is _currently_ selected in the
   * radio button list.
   */
  val isLanguageSelected: LiveData<Boolean> by lazy {
    Transformations.map(currentSelectedLanguage) { it == language }
  }
  /**
   * Returns the content description for the current language. This is used for accessibility
   * purposes to provide a readable description of the language in the UI.
   */
  fun languageContentDescription(): String {
    return when (language) {
      AudioLanguage.ARABIC_LANGUAGE -> appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.arabic_language_display_name_content_description
      )
      else -> languageDisplayName
    }
  }
}
