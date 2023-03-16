package org.oppia.android.app.testing

import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.viewmodel.ObservableViewModel

// TODO(#59): Make this view model only included in relevant tests instead of all prod builds.
/** [ObservableViewModel] for testing circular progress indicator adapters. */
class CircularProgressIndicatorAdaptersTestViewModel : ObservableViewModel() {
  /** The default minimum progress value to bind for circular progress indicators. */
  val defaultMinimum: Int = 0

  /** The default maximum progress value to bind for circular progress indicators. */
  val defaultMaximum: Int = 100

  /** The default progress value to bind for circular progress indicators. */
  val defaultInitialValue: Int = 10

  /**
   * A [MutableLiveData] that can be used to automatically change the progress of any circular
   * progress indicators that auto-bind to it for progress tracking.
   */
  val currentAutoProgress = MutableLiveData(defaultInitialValue)
}
