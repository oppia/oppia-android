package org.oppia.android.app.walkthrough.end

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying topic name in walkthrough final fragment. */
class WalkthroughFinalViewModel : ObservableViewModel() {
  val topicTitle = ObservableField<String>("")
}
