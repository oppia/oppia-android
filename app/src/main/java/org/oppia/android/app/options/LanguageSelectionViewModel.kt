package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

@FragmentScope
class LanguageSelectionViewModel @Inject constructor(
  val activity: AppCompatActivity
) :ObservableViewModel() {

  var audioLanguagesList = listOf<LanguageItemViewModel>(
    LanguageItemViewModel("No Audio"),
    LanguageItemViewModel("English"),
    LanguageItemViewModel("French"),
    LanguageItemViewModel("Hindi"),
    LanguageItemViewModel("Chinese")
  )


  val recyclerViewAudioList: List<LanguageItemViewModel> by lazy {
    audioLanguagesList
  }
}