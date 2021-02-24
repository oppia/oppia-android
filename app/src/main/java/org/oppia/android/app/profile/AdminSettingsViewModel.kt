package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AdminSettingsDialogFragment]. */
@FragmentScope
class AdminSettingsViewModel @Inject constructor() : ObservableViewModel() {
  val inputPin = ObservableField("")
  val errorMessage = ObservableField("")
}
