package org.oppia.app.home

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

/** [ViewModel] for user app usage history. */
class UserAppHistoryViewModel : HomeItemViewModel() {
  var isAppAlreadyOpened = ObservableField<Boolean>(false)
  var profileName : String = ""

  fun setAlreadyAppOpened(alreadyOpenedApp: Boolean) = isAppAlreadyOpened.set(alreadyOpenedApp)
}
