package org.oppia.android.app.onboarding.onboardingv2

import androidx.databinding.ObservableField
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel

/** The ViewModel for [NewLearnerProfileFragment]. */
@FragmentScope
class CreateLearnerProfileViewModel @Inject constructor() : ObservableViewModel() {
  val hasName = ObservableField(true)
}
