package org.oppia.app.onboarding

/** Listener for [OnboardingFragment] */
interface OnboardingListener {

  /** Skips onboarding slide. */
  fun clickOnSkip()

  /** Moves the user to the next onboarding slide. */
  fun clickOnNext()
}
