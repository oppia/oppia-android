package org.oppia.android.app.application

/** Provider for [ApplicationInjector]. The application context will implement this interface. */
interface ApplicationInjectorProvider {
  fun getApplicationInjector(): ApplicationInjector
}
