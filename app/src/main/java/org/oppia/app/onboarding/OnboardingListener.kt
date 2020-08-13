package org.oppia.app.onboarding

/** Listener for [OnboardingFragment] */
interface OnboardingListener {

  /** Skips onboarding slide. */
  fun clickOnSkip()

  /** Moves user to next onboarding slide. */
  fun clickOnNext()
}
