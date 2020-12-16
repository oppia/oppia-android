package org.oppia.android.app.options

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class LanguagesListViewModel @Inject constructor(
  fragment: Fragment
)
  : ObservableViewModel() {
  val audioLanguages : List<LanguageSelectionItemViewModel> by lazy {
    getAudioLanguagesList()
  }
  val arrayList = ArrayList<LanguageSelectionItemViewModel>()

  val audioLanguagesLiveData = MutableLiveData<List<LanguageSelectionItemViewModel>>()

  var preferenceValue = MutableLiveData<String>()


  init {
    arrayList.add(LanguageSelectionItemViewModel(fragment,"No Audio", false))
    arrayList.add(LanguageSelectionItemViewModel(fragment,"English", false))
    arrayList.add(LanguageSelectionItemViewModel(fragment,"French", false))
    arrayList.add(LanguageSelectionItemViewModel(fragment,"Hindi", true))
    arrayList.add(LanguageSelectionItemViewModel(fragment,"Chinese", true))
    audioLanguagesLiveData.postValue(getAudioLanguagesList())
  }


  fun updatePrefValue(title: String){
    preferenceValue.postValue(title)
  }

  fun getAudioLanguagesList(): ArrayList<LanguageSelectionItemViewModel>{
    return arrayList
  }

  fun selectedItem(title: String) {
    preferenceValue.postValue(title)
  }

  fun updateAudioLanguageList(title: String) {
    arrayList.forEach {
      it.isSelected = title==it.languageTitle
      Log.d("posted","${it.languageTitle} : ${it.isSelected}")
    }
    audioLanguagesLiveData.postValue(arrayList)
  }

  private val appLanguages: MutableList<OptionsAppLanguageViewModel> = ArrayList()

}