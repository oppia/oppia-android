package org.oppia.app.drawer

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for displaying User profile details in navigation header. */
class NavigationDrawerHeaderViewModel @Inject constructor() : ObservableViewModel() {
  val profileName = ObservableField<String>("")
}
