package org.oppia.app.options

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

/** Option items view model for the recyclerView in [OptionsFragment] */
abstract class OptionsItemViewModel : ObservableViewModel() {
  val isMultipaneOptions = ObservableField<Boolean>(false)
}
