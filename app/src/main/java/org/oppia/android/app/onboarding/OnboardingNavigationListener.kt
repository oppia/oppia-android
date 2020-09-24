package org.oppia.app.onboarding

/** Listener for buttons in OnboardingFragment */
interface OnboardingNavigationListener {

  /** Skips onboarding slide. */
  fun clickOnSkip()

  /** Moves the user to the next onboarding slide. */
  fun clickOnNext()
}
