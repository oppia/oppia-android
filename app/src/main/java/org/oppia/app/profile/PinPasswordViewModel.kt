package org.oppia.app.profile

import androidx.databinding.ObservableField
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

@ActivityScope
class PinPasswordViewModel @Inject constructor() : ObservableViewModel() {
  val showError = ObservableField(false)
  val showPassword = ObservableField(false)
}