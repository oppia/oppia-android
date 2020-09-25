package org.oppia.android.app.walkthrough

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [WalkthroughActivity]. */
class WalkthroughViewModel @Inject constructor() : ObservableViewModel() {
  val currentProgress = ObservableField(0)
}
