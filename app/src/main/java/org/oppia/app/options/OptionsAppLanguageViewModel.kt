package org.oppia.app.options

import androidx.databinding.ObservableField

/** App language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAppLanguageViewModel : OptionsItemViewModel(){
  val appLanguage = ObservableField<String>("English")
}
