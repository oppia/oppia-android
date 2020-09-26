package org.oppia.android.app.profileprogress

import androidx.databinding.ObservableField
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying user image in Activity. */
class ProfilePictureActivityViewModel : ObservableViewModel() {
  val profileAvatar = ObservableField<ProfileAvatar>()
}
