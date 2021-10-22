package org.oppia.android.app.profile

import android.content.Context
import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.R
import javax.inject.Inject

/** The ViewModel for [AddProfileActivity]. */
@ActivityScope
class AddProfileViewModel @Inject constructor(context: Context) : ObservableViewModel() {
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
  val starRequired: String = "*" + context.resources.getString(R.string.add_profile_required)

  fun clearAllErrorMessages() {
    pinErrorMsg.set("")
    confirmPinErrorMsg.set("")
    nameErrorMsg.set("")
  }
}
