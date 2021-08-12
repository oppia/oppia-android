package org.oppia.android.app.help

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Option items view model for the recyclerView in [HelpFragment] */
abstract class HelpViewModel : ObservableViewModel() {
  val isMultipane = ObservableField<Boolean>(false)
}
