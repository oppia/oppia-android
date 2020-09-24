package org.oppia.android.app.onboarding

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [OnboardingFragment]. */
class OnboardingViewModel @Inject constructor() : ObservableViewModel() {
  val slideNumber = ObservableField<Int>(0)
  val totalNumberOfSlides = TOTAL_NUMBER_OF_SLIDES

  fun slideChanged(slideIndex: Int) {
    slideNumber.set(slideIndex)
  }
}
