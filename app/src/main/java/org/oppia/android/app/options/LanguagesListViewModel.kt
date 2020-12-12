package org.oppia.android.app.options

import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class LanguagesListViewModel @Inject constructor()
  : ObservableViewModel() {
  val audioLanguages : List<LanguageSelectionItemViewModel> by lazy {
    getAudioLanguagesList()
  }

  fun getAudioLanguagesList(): ArrayList<LanguageSelectionItemViewModel>{
    val arrayList = ArrayList<LanguageSelectionItemViewModel>()
    arrayList.add(LanguageSelectionItemViewModel(this,"No Audio", false))
    arrayList.add(LanguageSelectionItemViewModel(this,"English", true))
    arrayList.add(LanguageSelectionItemViewModel(this,"French", false))
    arrayList.add(LanguageSelectionItemViewModel(this,"Hindi", false))
    arrayList.add(LanguageSelectionItemViewModel(this,"Chinese", false))
    return arrayList
  }

  private val appLanguages: MutableList<OptionsAppLanguageViewModel> = ArrayList()

}