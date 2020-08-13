package org.oppia.app.profile

import androidx.databinding.ObservableField
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AdminSettingsDialogFragment]. */
@FragmentScope
class AdminSettingsViewModel @Inject constructor() : ObservableViewModel() {
  val inputPin = ObservableField("")
  val errorMessage = ObservableField("")
}
