package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinViewModel @Inject constructor() : ObservableViewModel() {
  val pinErrorMsg = ObservableField("")
  val confirmErrorMsg = ObservableField("")
  val isAdmin = ObservableField(false)
  val inputPin = ObservableField("")
  val inputConfirmPin = ObservableField("")
  val isButtonActive = ObservableField(false)
}
