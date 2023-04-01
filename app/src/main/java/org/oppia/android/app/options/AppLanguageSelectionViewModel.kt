package org.oppia.android.app.options

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** Language list view model for the recycler view in [AppLanguageFragment]. */
@FragmentScope
class AppLanguageSelectionViewModel @Inject constructor(
  val fragment: Fragment
) : ObservableViewModel() {
  /** The name of the app language currently selected in the radio button list. */
  val selectedLanguage = MutableLiveData<String>()
  private val appLanguageRadioButtonListener = fragment as AppLanguageRadioButtonListener

  private val appLanguagesList = listOf(
    AppLanguageItemViewModel("English", selectedLanguage, appLanguageRadioButtonListener),
    AppLanguageItemViewModel("French", selectedLanguage, appLanguageRadioButtonListener),
    AppLanguageItemViewModel("Hindi", selectedLanguage, appLanguageRadioButtonListener),
    AppLanguageItemViewModel("Chinese", selectedLanguage, appLanguageRadioButtonListener)
  )

  /** The list of [AppLanguageItemViewModel]s which can be bound to a recycler view. */
  val recyclerViewAppLanguageList: List<AppLanguageItemViewModel> by lazy { appLanguagesList }
}
