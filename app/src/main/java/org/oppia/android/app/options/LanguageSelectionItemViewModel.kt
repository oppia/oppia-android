package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.viewmodel.ObservableViewModel

class LanguageSelectionItemViewModel(
  val fragment: Fragment,
  val languageTitle: String,
  var isSelected: Boolean
) : ObservableViewModel() {

  val checkBoxClickListener = fragment as CheckBoxClickListener

  fun onClick(title: String) {
    //save selected item
    isSelected = true
    checkBoxClickListener.updatePrefLanguage(title)
  }

}