package org.oppia.android.app.onboarding

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileViewModel @Inject constructor() : ObservableViewModel() {

  /** ObservableField that tracks whether creating a nickname has triggered an error condition. */
  val hasErrorMessage = ObservableField(false)
}
