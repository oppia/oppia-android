package org.oppia.app.profile

import androidx.lifecycle.MutableLiveData
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AdminPinActivity]. */
@ActivityScope
class AdminPinViewModel @Inject constructor() : ObservableViewModel() {
  val pinErrorMsg = MutableLiveData("")
  val confirmPinErrorMsg = MutableLiveData("")
  val savedPin = MutableLiveData("")
  val savedConfirmPin = MutableLiveData("")
}
