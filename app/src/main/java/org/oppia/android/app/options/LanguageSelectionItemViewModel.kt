package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.viewmodel.ObservableViewModel

class LanguageSelectionItemViewModel(
  val activity: AppCompatActivity,
  val languageTitle: String,
  var isSelected: Boolean
) : ObservableViewModel() {

  val checkBoxClickListener = activity as CheckBoxClickListener

  fun onClick(title: String) {
    //save selected item
    isSelected = true
    checkBoxClickListener.updatePrefLanguage(title)
  }

}