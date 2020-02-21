package org.oppia.app.home

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

/** [ViewModel] for user app usage history. */
class WelcomeViewModel : HomeItemViewModel() {
  var isAppAlreadyOpened = ObservableField<Boolean>(false)
  var profileName : String = ""

}
