package org.oppia.app.walkthrough.welcome

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import org.oppia.app.model.Profile
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for displaying User profile details in walkthrough Welcom Fragment. */
class WalkthroughWelcomeViewModel @Inject constructor() : ObservableViewModel() {

  val profile = ObservableField<Profile>(Profile.getDefaultInstance())

}