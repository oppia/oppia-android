package org.oppia.android.app.onboarding

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

private const val INITIAL_SLIDE_NUMBER = 0

/** [ViewModel] for [OnboardingFragment]. */
class OnboardingViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  val slideNumber = ObservableField(INITIAL_SLIDE_NUMBER)
  val totalNumberOfSlides = TOTAL_NUMBER_OF_SLIDES
  val slideDotsContainerContentDescription =
    ObservableField(computeSlideDotsContainerContentDescription(INITIAL_SLIDE_NUMBER))

  fun slideChanged(slideIndex: Int) {
    slideNumber.set(slideIndex)
    slideDotsContainerContentDescription.set(
      computeSlideDotsContainerContentDescription(slideIndex)
    )
  }

  private fun computeSlideDotsContainerContentDescription(slideNumber: Int): String {
    return resourceHandler.getStringInLocale(
      R.string.onboarding_slide_dots_content_description, slideNumber + 1, totalNumberOfSlides
    )
  }
}
