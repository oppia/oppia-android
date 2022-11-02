package org.oppia.android.app.onboarding

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler

const val TOTAL_NUMBER_OF_SLIDES = 4

/** [ViewModel] for slide in onboarding flow. */
class OnboardingSlideViewModel(
  val context: Context,
  viewPagerSlide: ViewPagerSlide,
  private val resourceHandler: AppLanguageResourceHandler
) : OnboardingViewPagerViewModel() {
  val slideImage = ObservableField(R.drawable.ic_portrait_onboarding_0)
  val title =
    ObservableField(getOnboardingSlide0Title())
  val description =
    ObservableField(
      resourceHandler.getStringInLocale(
        R.string.onboarding_activity_onboarding_slide_0_description
      )
    )
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
        title.set(getOnboardingSlide0Title())
        description.set(
          resourceHandler.getStringInLocale(
            R.string.onboarding_activity_onboarding_slide_0_description
          )
        )
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
        title.set(
          resourceHandler.getStringInLocale(
            R.string.onboarding_activity_onboarding_slide_1_title
          )
        )
        description.set(
          resourceHandler.getStringInLocale(
            R.string.onboarding_activity_onboarding_slide_1_description
          )
        )
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
        title.set(
          resourceHandler.getStringInLocale(
            R.string.onboarding_activity_onboarding_slide_2_title
          )
        )
        description.set(
          resourceHandler.getStringInLocale(
            R.string.onboarding_activity_onboarding_slide_2_description
          )
        )
      }
    }
  }

  private fun getOnboardingSlide0Title(): String {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.onboarding_activity_onboarding_slide_0_title, appName
    )
  }
}
