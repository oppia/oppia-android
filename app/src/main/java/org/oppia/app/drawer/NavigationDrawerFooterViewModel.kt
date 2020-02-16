package org.oppia.app.drawer

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class NavigationDrawerFooterViewModel @Inject constructor() : ObservableViewModel() {
  val isAdmin = ObservableField<Boolean>(false)
}
