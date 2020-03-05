package org.oppia.app.walkthrough

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

class WalkthroughViewModel : ObservableViewModel() {
  private var currentProgress = ObservableField(0)
}