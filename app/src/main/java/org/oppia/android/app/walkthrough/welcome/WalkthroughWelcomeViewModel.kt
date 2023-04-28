package org.oppia.android.app.walkthrough.welcome

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** View model for displaying user profile details in walkthrough welcome fragment. */
class WalkthroughWelcomeViewModel : ObservableViewModel() {
  val profileName = ObservableField<String>("")
}
