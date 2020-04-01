package org.oppia.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameViewModel @Inject constructor() : ObservableViewModel() {
  val inputName = ObservableField("")
  val nameErrorMsg = ObservableField("")
}
