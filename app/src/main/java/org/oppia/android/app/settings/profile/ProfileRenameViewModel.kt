package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameViewModel @Inject constructor() : ObservableViewModel() {

  /** Input name for new profile name. */
  val inputName = ObservableField("")

  /** Error message for the text input layout in [ProfileRenameFragment]. */
  val nameErrorMsg = ObservableField("")
}
