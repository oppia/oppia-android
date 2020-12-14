package org.oppia.android.app.options

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class LanguagesListViewModel @Inject constructor(
  val activity: AppCompatActivity
)
  : ObservableViewModel() {
  val audioLanguages : List<LanguageSelectionItemViewModel> by lazy {
    getAudioLanguagesList()
  }
  val arrayList = ArrayList<LanguageSelectionItemViewModel>()

  val audioLanguagesLiveData = MutableLiveData<List<LanguageSelectionItemViewModel>>()

  var preferenceValue = MutableLiveData<String>()


  init {
    arrayList.add(LanguageSelectionItemViewModel(activity,"No Audio", false))
    arrayList.add(LanguageSelectionItemViewModel(activity,"English", false))
    arrayList.add(LanguageSelectionItemViewModel(activity,"French", false))
    arrayList.add(LanguageSelectionItemViewModel(activity,"Hindi", true))
    arrayList.add(LanguageSelectionItemViewModel(activity,"Chinese", true))
    audioLanguagesLiveData.postValue(getAudioLanguagesList())
  }


  fun updatePrefValue(title: String){

    Log.d("posted", "vrooo ;( ;(")
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
    }
    audioLanguagesLiveData.postValue(arrayList)
    Log.d("posted", "vroo ;(")
  }

  private val appLanguages: MutableList<OptionsAppLanguageViewModel> = ArrayList()

}