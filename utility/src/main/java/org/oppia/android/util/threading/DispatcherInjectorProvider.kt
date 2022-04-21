package org.oppia.android.util.threading

/** Provider for [DispatcherInjector]s. To be implemented by the application class. */
interface DispatcherInjectorProvider {
  /** Returns the [DispatcherInjector] corresponding to the current application context. */
  fun getDispatcherInjector(): DispatcherInjector
}
