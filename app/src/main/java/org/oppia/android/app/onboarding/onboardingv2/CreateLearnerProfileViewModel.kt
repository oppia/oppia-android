package org.oppia.android.app.onboarding.onboardingv2

import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [CreateProfileFragment]. */
@FragmentScope
class CreateLearnerProfileViewModel @Inject constructor() : ObservableViewModel() {
  private val orientation = Resources.getSystem().configuration.orientation

  /** ObservableField that tracks whether a nickname has been entered. */
  val hasError = ObservableField(true)

  val onboardingStepsCount =
    if (orientation == Configuration.ORIENTATION_PORTRAIT) View.VISIBLE else View.GONE
}
