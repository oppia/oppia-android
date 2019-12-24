package org.oppia.app.onboarding

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel

const val TOTAL_NUMBER_OF_SLIDES = 4

/** [ViewModel] for slide in onboarding flow. */
class OnboardingSlideViewModel(val context: Context, viewPagerSlide: ViewPagerSlide) : ObservableViewModel() {
  val slideImage = ObservableField<Int>(R.drawable.ic_onboarding_0)
  val title = ObservableField<String>(context.resources.getString(R.string.onboarding_slide_0_title))
  val description = ObservableField<String>(context.resources.getString(R.string.onboarding_slide_0_description))

  init {
    initializingSlide(viewPagerSlide)
  }

  private fun initializingSlide(viewPagerSlide: ViewPagerSlide) {
    when (viewPagerSlide) {
      ViewPagerSlide.SLIDE_0 -> {
        slideImage.set(R.drawable.ic_onboarding_0)
        title.set(context.resources.getString(R.string.onboarding_slide_0_title))
        description.set(context.resources.getString(R.string.onboarding_slide_0_description))
      }
      ViewPagerSlide.SLIDE_1 -> {
        slideImage.set(R.drawable.ic_onboarding_1)
        title.set(context.resources.getString(R.string.onboarding_slide_1_title))
        description.set(context.resources.getString(R.string.onboarding_slide_1_description))
      }
      ViewPagerSlide.SLIDE_2 -> {
        slideImage.set(R.drawable.ic_onboarding_2)
        title.set(context.resources.getString(R.string.onboarding_slide_2_title))
        description.set(context.resources.getString(R.string.onboarding_slide_2_description))
      }
      ViewPagerSlide.SLIDE_3 -> {
        slideImage.set(R.drawable.ic_onboarding_3)
        title.set(context.resources.getString(R.string.onboarding_slide_3_title))
        description.set(context.resources.getString(R.string.onboarding_slide_3_description))
      }
    }
  }
}
