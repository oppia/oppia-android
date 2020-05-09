package org.oppia.app.onboarding

/** Enum to store the slides of [OnboardingFragment] and get slide by position. */
enum class ViewPagerSlide(private var position: Int) {
  SLIDE_0(position = 0),
  SLIDE_1(position = 1),
  SLIDE_2(position = 2),
  SLIDE_3(position = 3);

  companion object {
    fun getSlideForPosition(position: Int): ViewPagerSlide {
      val ordinal = checkNotNull(values().map(ViewPagerSlide::position)[position]) {
        "No tab corresponding to position: $position"
      }
      return values()[ordinal]
    }
  }
}
