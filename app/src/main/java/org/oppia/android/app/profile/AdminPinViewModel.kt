package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AdminPinActivity]. */
@ActivityScope
class AdminPinViewModel @Inject constructor() : ObservableViewModel() {
  val pinErrorMsg = ObservableField("")
  val confirmPinErrorMsg = ObservableField("")
  val savedPin = ObservableField("")
  val savedConfirmPin = ObservableField("")
  val isButtonActive = ObservableField(false)
}
