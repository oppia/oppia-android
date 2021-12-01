package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinViewModel @Inject constructor() : ObservableViewModel() {
  /** variable for storing error message in first text input layout. */
  val pinErrorMsg = ObservableField("")

  /** variable for storing error message in second text input layout. */
  val confirmErrorMsg = ObservableField("")

  /** for confirming if the profile is admin or not. */
  val isAdmin = ObservableField(false)

  /** for storing input pin entered by user in first text input layout. */
  val inputPin = ObservableField("")

  /** for storing input pin entered by user in second text input layout. */
  val inputConfirmPin = ObservableField("")

  /** for verification if both pins are same then only enable the button. */
  val isButtonActive = ObservableField(false)
}
