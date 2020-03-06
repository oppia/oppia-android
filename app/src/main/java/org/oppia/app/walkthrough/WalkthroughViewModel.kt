package org.oppia.app.walkthrough

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class WalkthroughViewModel @Inject constructor(): ObservableViewModel() {
  var currentProgress = ObservableField(0)
}
