package org.oppia.android.app.onboardingv2

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileViewModel @Inject constructor() : ObservableViewModel() {

  /** ObservableField that tracks whether a nickname has been entered. */
  val hasError = ObservableField(false)
}
