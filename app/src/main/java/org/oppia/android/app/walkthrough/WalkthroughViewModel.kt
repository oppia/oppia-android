package org.oppia.android.app.walkthrough

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** View model for [WalkthroughActivity]. */
class WalkthroughViewModel @Inject constructor() : ObservableViewModel() {
  val currentProgress = ObservableField(0)
}
