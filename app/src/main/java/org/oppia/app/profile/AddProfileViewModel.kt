package org.oppia.app.profile

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
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

  var isSubmitButtonActive = ObservableField<Boolean>(false)

  fun clearAllErrorMessages() {
    pinErrorMsg.set("")
    confirmPinErrorMsg.set("")
    nameErrorMsg.set("")
  }

  @Bindable
  fun getInputPinTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        isSubmitButtonActive.set(answer.toString().trim().length == 5)
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }
}
