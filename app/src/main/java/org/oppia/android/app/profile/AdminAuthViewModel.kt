package org.oppia.android.app.profile

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthViewModel @Inject constructor() : ObservableViewModel() {
  val inputPin = ObservableField<String>("")
  val errorMessage = ObservableField<String>("")

  var isSubmitButtonActive = ObservableField<Boolean>(false)

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
