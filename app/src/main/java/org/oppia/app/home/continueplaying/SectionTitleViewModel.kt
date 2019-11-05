package org.oppia.app.home.continueplaying

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for user app usage history. */
@FragmentScope
class SectionTitleViewModel @Inject constructor() : HomeItemViewModel() {
  var isAppAlreadyOpened = ObservableField<Boolean>(false)

  fun setAlreadyAppOpened(alreadyOpenedApp: Boolean) {
    isAppAlreadyOpened.set(alreadyOpenedApp)
  }
}
