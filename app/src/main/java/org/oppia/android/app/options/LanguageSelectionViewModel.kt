package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/**
 * Language list view model for the recycler view in [AppLanguageFragment] and
 * [AudioLanguageFragment].
 */
@FragmentScope
class LanguageSelectionViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {

  val selectedLanguage = MutableLiveData<String>()

  private val appLanguagesList = listOf<LanguageItemViewModel>(
    LanguageItemViewModel("English"),
    LanguageItemViewModel("French"),
    LanguageItemViewModel("Hindi"),
    LanguageItemViewModel("Chinese")
  )
  private val audioLanguagesList = listOf<LanguageItemViewModel>(
    LanguageItemViewModel("No Audio"),
    LanguageItemViewModel("English"),
    LanguageItemViewModel("French"),
    LanguageItemViewModel("Hindi"),
    LanguageItemViewModel("Chinese")
  )

  val recyclerViewAudioLanguageList: List<LanguageItemViewModel> by lazy {
    audioLanguagesList
  }

  val recyclerViewAppLanguageList: List<LanguageItemViewModel> by lazy {
    appLanguagesList
  }
}
