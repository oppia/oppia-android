package org.oppia.android.app.drawer

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for displaying Administrator Controls option. */
class NavigationDrawerFooterViewModel @Inject constructor() : ObservableViewModel() {
  val isDebugMode = ObservableField<Boolean>(false)
  val isDeveloperOptionsSelected = ObservableField<Boolean>(false)
  val isAdmin = ObservableField<Boolean>(false)
  val isAdministratorControlsSelected = ObservableField<Boolean>(false)
}
