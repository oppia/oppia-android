package org.oppia.app.profileprogress

import android.net.Uri
import androidx.databinding.ObservableField
import org.oppia.app.model.ProfileAvatar
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying User image in Activity. */
class ProfilePictureActivityViewModel : ObservableViewModel() {
  val profileAvatar = ObservableField<ProfileAvatar>()

}
