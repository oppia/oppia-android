package org.oppia.app.drawer

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class NavigationDrawerHeaderViewModel @Inject constructor() : ObservableViewModel() {
  val profileName = ObservableField<String>("")
}
