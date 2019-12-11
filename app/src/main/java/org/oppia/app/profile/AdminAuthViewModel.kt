package org.oppia.app.profile

import androidx.databinding.ObservableField
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthViewModel @Inject constructor() : ObservableViewModel() {
  val errorMessage = ObservableField("")
}
