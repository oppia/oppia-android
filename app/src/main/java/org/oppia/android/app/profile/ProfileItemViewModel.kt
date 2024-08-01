package org.oppia.android.app.profile

import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel

/** ViewModel for binding a profile data to the UI. */
class ProfileItemViewModel(
  val profile: Profile,
  val onProfileClicked: (Profile) -> Unit
) : ObservableViewModel() {

  /** Called when a profile is clicked. */
  // todo maybe remove
  fun profileClicked() {
    onProfileClicked(profile)
  }
}
