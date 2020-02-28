package org.oppia.app.options

import androidx.databinding.ObservableField

/** Audio Language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAudioLanguageViewModel : OptionsItemViewModel(){
  val audioLanguage = ObservableField<String>("No Audio")
}

