package org.oppia.app.home

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for user app usage history. */
@FragmentScope
class UserAppHistoryViewModel @Inject constructor() : ObservableViewModel() {

  var isAppAlreadyOpened = ObservableField<Boolean>(false)

  fun setAlreadyAppOpened(alreadyOpenedApp: Boolean) {
    isAppAlreadyOpened.set(alreadyOpenedApp)
  }
}
