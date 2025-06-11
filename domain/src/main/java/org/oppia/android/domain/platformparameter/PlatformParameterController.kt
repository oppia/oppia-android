package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.Deferred
import org.oppia.android.util.data.DataProvider

/** Controller for managing and synchronizing platform parameters and feature flags. */
interface PlatformParameterController {
  /**
   * Ensures platform parameter and feature flag states are properly loaded and able to be directly
   * injected through Dagger.
   *
   * This method *must* be called before attempting to inject any platform parameters or feature
   * flags. Note that platform parameters and feature flags will not have state changes after this
   * method is called for the lifetime of the process.
   *
   * It's safe to call this method more than once, even after loading, as the returned [Deferred]
   * will be the same across calls and complete when parameters have finished loading. For UI-bound
   * observers of this state, use [getParameterInitializationStatus].
   *
   * @return a [Deferred] that indicates the completion of loading parameters. Note that the
   *    [Deferred] will fail if something went wrong. Also, the actual value returned does not have
   *    any significant meaning.
   */
  fun loadParametersAsync(): Deferred<Unit>

  /**
   * Returns a [DataProvider] that will switch from a value of 'false' to 'true' when parameters
   * have finished loading.
   *
   * This method and the returned [DataProvider] are safe to use for the lifetime of the process,
   * even before [loadParametersAsync] is called. Calling this method will not initiate any actual
   * initialization--[loadParametersAsync] must be used for that, instead.
   *
   * The returned [DataProvider] will never return a pending state, nor will it represent the
   * failure state if parameter loading via [loadParametersAsync] fails.
   */
  fun getParameterInitializationStatus(): DataProvider<Boolean>

  /**
   * Downloads all Android app-specific platform parameters and feature flag states from the remote
   * Oppia web server.
   *
   * This method must be called after [loadParametersAsync] and, specifically, after parameters
   * have finished loading.
   *
   * Note that the parameter and flag states downloaded from Oppia web will be saved locally but
   * will not affect parameter or flag state in the currently running app process. These changes do
   * not take effect until the next time [loadParametersAsync] is called (that is, upon the next app
   * start).
   *
   * Note that the returned [DataProvider] will never have a pending state and will never have more
   * than one success/fail result.
   *
   * @return a [DataProvider] that indicates the success/failure of downloading parameters. Note
   *    that the actual value returned does not have any significant meaning.
   */
  fun downloadRemoteParameters(): DataProvider<Unit>
}
