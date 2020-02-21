package org.oppia.app.profile

import androidx.databinding.ObservableField
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ResetPinDialogFragment]. */
@FragmentScope
class ResetPinViewModel @Inject constructor() : ObservableViewModel() {
  val errorMessage = ObservableField("")
}
