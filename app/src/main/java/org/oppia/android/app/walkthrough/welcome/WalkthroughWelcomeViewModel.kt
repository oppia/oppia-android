package org.oppia.android.app.walkthrough.welcome

import androidx.databinding.ObservableField
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying user profile details in walkthrough welcome fragment. */
class WalkthroughWelcomeViewModel : ObservableViewModel() {
  val profileName = ObservableField<String>("")
}
