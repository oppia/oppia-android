package org.oppia.android.domain.platformparameter

/** Provider for [PlatformParameterControllerInjector]s. To be implemented by the application class. */
interface PlatformParameterControllerInjectorProvider {
  /**
   * Returns the [PlatformParameterControllerInjector] corresponding to the current application
   * context.
   */
  fun getPlatformParameterControllerInjector(): PlatformParameterControllerInjector
}
