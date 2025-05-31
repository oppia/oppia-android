package org.oppia.android.domain.platformparameter

import org.oppia.android.util.data.DataProvider
import org.oppia.android.app.model.PlatformParameter

/** Controller for managing and synchronizing platform parameters and feature flags. */
interface PlatformParameterController {
  /**
   * Ensures platform parameter and feature flag states are properly loaded and able to be directly
   * injected through Dagger.
   *
   * This method *must* be called before attempting to inject any platform parameters or feature
   * flags. Note that platform parameters and feature flags will not have state changes after this
   * method is called for the lifetime of the process. This method also should never be called more
   * than once, and subsequent calls will trigger an exception to be thrown.
   *
   * Note that the returned [DataProvider] will never have a pending state and will never have more
   * than one success/fail result.
   *
   * @return a [DataProvider] that indicates the success/failure of parameter loading. Note that the
   *    actual value returned does not have any significant meaning.
   */
  fun loadParameters(): DataProvider<Any?>

  /**
   * Downloads all Android app-specific platform parameters and feature flag states from the remote
   * Oppia web server.
   *
   * Note that the parameter and flag states downloaded from Oppia web will be saved locally but
   * will not affect parameter or flag state in the currently running app process. These changes do
   * not take effect until the next time [loadParameters] is called (that is, upon the next app
   * start).
   *
   * Note that the returned [DataProvider] will never have a pending state and will never have more
   * than one success/fail result.
   *
   * @return a [DataProvider] that indicates the success/failure of downloading parameters. Note
   *    that the actual value returned does not have any significant meaning.
   */
  fun downloadRemoteParameters(): DataProvider<Any?>

  // TODO: Figure out what to do with this (remove or add TODO to remove).
  /**
   * Updates the platform parameter database in cache store.
   *
   * @param platformParameterList list of [PlatformParameter] objects which needs to be cached
   * @return a [DataProvider] that indicates the success/failure of this update operation
   */
  fun updatePlatformParameterDatabase(
    platformParameterList: List<PlatformParameter>
  ): DataProvider<Any?>

  // TODO: Figure out what to do with this (remove or add TODO to remove).
  /**
   * Returns a [DataProvider] which can be used to confirm that PlatformParameterDatabase read
   * process has been completed.
   */
  fun getParameterDatabase(): DataProvider<Unit>
}
