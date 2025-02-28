package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import javax.inject.Inject
import javax.inject.Singleton

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
}
