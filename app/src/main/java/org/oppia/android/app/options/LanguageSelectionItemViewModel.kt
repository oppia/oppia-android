package org.oppia.android.app.options

import androidx.lifecycle.ViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel

class LanguageSelectionItemViewModel(
  val languagesListViewModel: LanguagesListViewModel,
  val languageTitle: String,
  var isSelected: Boolean
) : ObservableViewModel() {
  fun onClick(title: String) {
    //save selected item
    isSelected = true
  }

}