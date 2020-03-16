package org.oppia.app.walkthrough.welcome

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying User profile details in walkthrough Welcome Fragment. */
class WalkthroughWelcomeViewModel : ObservableViewModel() {
  val profileName = ObservableField<String>("")
}
