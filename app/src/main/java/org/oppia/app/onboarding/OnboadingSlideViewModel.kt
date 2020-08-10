package org.oppia.app.onboarding

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel

const val TOTAL_NUMBER_OF_SLIDES = 4

/** [ViewModel] for slide in onboarding flow. */
class OnboardingSlideViewModel(val context: Context, viewPagerSlide: ViewPagerSlide) :
  ObservableViewModel() {
  val slideImage = ObservableField<Int>(R.drawable.ic_portrait_onboarding_0)
  val contentDescription =
    ObservableField<String>(context.resources.getString(R.string.onboarding_slide_0_title))
  val title =
    ObservableField<String>(context.resources.getString(R.string.onboarding_slide_0_title))
  val description =
    ObservableField<String>(context.resources.getString(R.string.onboarding_slide_0_description))
  private val orientation = Resources.getSystem().configuration.orientation

  init {
    initializingSlide(viewPagerSlide)
  }

  private fun initializingSlide(viewPagerSlide: ViewPagerSlide) {
    when (viewPagerSlide) {
      ViewPagerSlide.SLIDE_0 -> {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          if (context.resources.getBoolean(R.bool.isTablet)) {
            slideImage.set(R.drawable.ic_landscape_onboarding_0_tablet)
          } else {
            slideImage.set(R.drawable.ic_landscape_onboarding_0)
          }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          slideImage.set(R.drawable.ic_portrait_onboarding_0)
        }
        contentDescription.set(context.resources.getString(R.string.onboarding_slide_0_title))
        title.set(context.resources.getString(R.string.onboarding_slide_0_title))
        description.set(context.resources.getString(R.string.onboarding_slide_0_description))
      }
      ViewPagerSlide.SLIDE_1 -> {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          if (context.resources.getBoolean(R.bool.isTablet)) {
            slideImage.set(R.drawable.ic_landscape_onboarding_1_tablet)
          } else {
            slideImage.set(R.drawable.ic_landscape_onboarding_1)
          }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          slideImage.set(R.drawable.ic_portrait_onboarding_1)
        }
        contentDescription.set(context.resources.getString(R.string.onboarding_slide_1_title))
        title.set(context.resources.getString(R.string.onboarding_slide_1_title))
        description.set(context.resources.getString(R.string.onboarding_slide_1_description))
      }
      ViewPagerSlide.SLIDE_2 -> {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          if (context.resources.getBoolean(R.bool.isTablet)) {
            slideImage.set(R.drawable.ic_landscape_onboarding_2_tablet)
          } else {
            slideImage.set(R.drawable.ic_landscape_onboarding_2)
          }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          slideImage.set(R.drawable.ic_portrait_onboarding_2)
        }
        contentDescription.set(context.resources.getString(R.string.onboarding_slide_2_title))
        title.set(context.resources.getString(R.string.onboarding_slide_2_title))
        description.set(context.resources.getString(R.string.onboarding_slide_2_description))
      }
    }
  }
}
