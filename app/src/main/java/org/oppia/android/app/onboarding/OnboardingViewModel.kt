package org.oppia.android.app.onboarding

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [OnboardingFragment]. */
class OnboardingViewModel @Inject constructor() : ObservableViewModel() {
  val slideNumber = ObservableField<Int>(0)
  val totalNumberOfSlides = TOTAL_NUMBER_OF_SLIDES
  val slideDotsContentDescription = ObservableField<String>("Slide 1 of 4")

  fun slideChanged(slideIndex: Int) {
    slideNumber.set(slideIndex)
    setSlideDotsContentDescription(slideIndex)
  }

  private fun setSlideDotsContentDescription(slideIndex: Int) {
    slideDotsContentDescription.set("Slide ${slideIndex + 1} of 4")
  }
}
