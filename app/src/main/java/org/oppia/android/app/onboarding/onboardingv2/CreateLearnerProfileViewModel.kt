package org.oppia.android.app.onboarding.onboardingv2

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [NewLearnerProfileFragment]. */
@FragmentScope
class CreateLearnerProfileViewModel @Inject constructor() : ObservableViewModel() {
  /** ObservableField that tracks whether a nickname has been entered. */
  val hasName = ObservableField(true)
}
