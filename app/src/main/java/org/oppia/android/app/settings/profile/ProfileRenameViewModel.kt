package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameViewModel @Inject constructor() : ObservableViewModel() {

  /** The name of the profile. */
  val inputName = ObservableField("")

  /** The error message of the wrong name entered in [ProfileRenameFragment]. */
  val nameErrorMsg = ObservableField("")
}
