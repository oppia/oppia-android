package org.oppia.app.walkthrough.end

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying topic name in walkthrough final fragment. */
class WalkthroughFinalViewModel : ObservableViewModel() {
  val topicTitle = ObservableField<String>("")
}
