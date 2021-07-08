package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Super-class for generalising different views for the recyclerView in [DeveloperOptionsFragment]. */
abstract class DeveloperOptionsItemViewModel : ObservableViewModel() {
  val itemIndex = ObservableField<Int>()
}
