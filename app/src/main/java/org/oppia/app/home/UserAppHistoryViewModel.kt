package org.oppia.app.home

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

/** [ViewModel] for user app usage history. */
class UserAppHistoryViewModel : HomeItemViewModel() {
  var isAppAlreadyOpened = ObservableField<Boolean>(false)

  fun setAlreadyAppOpened(alreadyOpenedApp: Boolean) = isAppAlreadyOpened.set(alreadyOpenedApp)
  var isAppAlreadyOnboarded = ObservableField<Boolean>(false)

  fun setAlreadyAppOnboarded(isalreadyOnboardedApp: Boolean) = isAppAlreadyOnboarded.set(isalreadyOnboardedApp)
}
