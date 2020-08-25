package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

/** Super-class for generalising different views for the recyclerView in [AdministratorControlsFragment] */
abstract class AdministratorControlsItemViewModel : ObservableViewModel() {
  val isMultipane = ObservableField<Boolean>(false)
  val itemIndex = ObservableField<Int>()
}
