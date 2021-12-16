package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinViewModel @Inject constructor() : ObservableViewModel() {
  /** The error message shown if the second pin not matches with the first pin. */
  val pinErrorMsg = ObservableField("")

  /** The second pin to which the first pin is matched. */
  val confirmErrorMsg = ObservableField("")

  /** Whether the user is an admin. */
  val isAdmin = ObservableField(false)

  /** The new pin inputted by the user. */
  val inputPin = ObservableField("")

  /** The second PIN inputted by the user
   *  (is expected to be the same as [inputPin] to confirm the user knows this new number).
   */
  val inputConfirmPin = ObservableField("")

  /** Whether the save pin button is enabled (i.e. there is no issue with the inputted PINs.) */
  val isButtonActive = ObservableField(false)
}
