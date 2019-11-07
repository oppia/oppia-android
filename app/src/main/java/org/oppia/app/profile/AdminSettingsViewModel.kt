package org.oppia.app.profile

import androidx.databinding.ObservableField
import org.oppia.app.activity.ActivityScope
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

@FragmentScope
class AdminSettingsViewModel @Inject constructor() : ObservableViewModel() {
  val errorMessage = ObservableField("")
}