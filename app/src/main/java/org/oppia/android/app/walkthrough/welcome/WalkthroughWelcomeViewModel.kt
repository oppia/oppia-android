package org.oppia.app.walkthrough.welcome

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying user profile details in walkthrough welcome fragment. */
class WalkthroughWelcomeViewModel : ObservableViewModel() {
  val profileName = ObservableField<String>("")
}
