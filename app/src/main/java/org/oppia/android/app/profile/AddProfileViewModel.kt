package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AddProfileActivity]. */
@ActivityScope
class AddProfileViewModel @Inject constructor(
  resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
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
  val requiredField: String =
    resourceHandler.getStringInLocale(R.string.add_profile_required)

  fun clearAllErrorMessages() {
    pinErrorMsg.set("")
    confirmPinErrorMsg.set("")
    nameErrorMsg.set("")
  }
}
