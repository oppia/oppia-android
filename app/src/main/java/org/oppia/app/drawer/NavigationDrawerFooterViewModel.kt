package org.oppia.app.drawer

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for displaying Administrator Controls option. */
class NavigationDrawerFooterViewModel @Inject constructor() : ObservableViewModel() {
  val isAdmin = ObservableField<Boolean>(false)
  val isAdministratorControlsSelected = ObservableField<Boolean>(false)
}
