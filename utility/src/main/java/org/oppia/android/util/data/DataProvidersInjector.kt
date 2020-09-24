package org.oppia.util.data

/** Injector for [DataProviders]. Implemented by a generated Dagger application component. */
interface DataProvidersInjector {
  /** Returns the [DataProviders] corresponding to the current application context. */
  fun getDataProviders(): DataProviders
}
