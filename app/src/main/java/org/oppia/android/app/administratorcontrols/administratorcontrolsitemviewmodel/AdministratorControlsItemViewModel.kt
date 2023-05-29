package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Super-class for generalising different views for the recyclerView in [AdministratorControlsFragment] */
abstract class AdministratorControlsItemViewModel : ObservableViewModel() {
  /** [Boolean] observable value showing if [View] is multipane. */
  val isMultipane = ObservableField<Boolean>(false)
  /** [Int] representing the index of items bind in [AdministratorControlsActivity]. */
  val itemIndex = ObservableField<Int>()
}
