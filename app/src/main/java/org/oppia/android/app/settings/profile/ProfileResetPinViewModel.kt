package org.oppia.android.app.settings.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinViewModel @Inject constructor() : ObservableViewModel() {
  /** The error message is shown if the second pin not matches with the first pin. */
  val pinErrorMsg = ObservableField("")

  /** The error message is confirmed with this pin. */
  val confirmErrorMsg = ObservableField("")

  /** If the user is admin then he should have admin rights. */
  val isAdmin = ObservableField(false)

  /** This is used to store the user pin. */
  val inputPin = ObservableField("")

  /** This confirms and validates the previous pin entered by the user. */
  val inputConfirmPin = ObservableField("")

  /** The submit button only becomes clickable when both the pins are same. */
  val isButtonActive = ObservableField(false)
}
