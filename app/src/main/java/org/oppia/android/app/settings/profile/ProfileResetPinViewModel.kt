package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinViewModel @Inject constructor() : ObservableViewModel() {
  /** Variable for storing error message in first text input layout. */
  val pinErrorMsg = ObservableField("")

  /** Variable for storing error message in second text input layout. */
  val confirmErrorMsg = ObservableField("")

  /** For confirming if the profile is admin or not. */
  val isAdmin = ObservableField(false)

  /** For storing input pin entered by user in first text input layout. */
  val inputPin = ObservableField("")

  /** For storing input pin entered by user in second text input layout. */
  val inputConfirmPin = ObservableField("")

  /** For verification if both pins are same then only enable the button. */
  val isButtonActive = ObservableField(false)
}
