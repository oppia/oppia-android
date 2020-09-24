package org.oppia.android.app.options

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Option items view model for the recyclerView in [OptionsFragment] */
abstract class OptionsItemViewModel : ObservableViewModel() {
  val isMultipane = ObservableField<Boolean>(false)
  val itemIndex = ObservableField<Int>()
}
