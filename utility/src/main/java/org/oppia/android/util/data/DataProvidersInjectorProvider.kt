package org.oppia.android.util.data

/** Provider for [DataProvidersInjector]s. To be implemented by the application class. */
interface DataProvidersInjectorProvider {
  /** Returns the [DataProvidersInjector] corresponding to the current application context. */
  fun getDataProvidersInjector(): DataProvidersInjector
}
