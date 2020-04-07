package org.oppia.app.profile

import androidx.databinding.ObservableField
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AddProfileActivity]. */
@ActivityScope
class AddProfileViewModel @Inject constructor() : ObservableViewModel() {
  val validPin = ObservableField(false)
  val pinErrorMsg = ObservableField("")
  val confirmPinErrorMsg = ObservableField("")
  val nameErrorMsg = ObservableField("")
  val inputName = ObservableField("")
  val inputPin = ObservableField("")
  val inputConfirmPin = ObservableField("")
  val createPin = ObservableField(false)
  val isButtonActive = ObservableField(false)
  val showInfoAlertPopup = ObservableField<Boolean>(false)

  fun clearAllErrorMessages() {
    pinErrorMsg.set("")
    confirmPinErrorMsg.set("")
    nameErrorMsg.set("")
  }
}
