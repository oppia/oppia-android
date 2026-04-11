package org.oppia.android.testing.system

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.system.AppTerminationManager

/**
 * Fake implementation of [AppTerminationManager] that throws an [ExitException] instead of
 * terminating the process. This allows tests to verify that app termination was requested without
 * actually killing the test process, and enables JaCoCo to properly record coverage.
 */
@Singleton
class FakeAppTerminationManager @Inject constructor() : AppTerminationManager {
  override fun forceCloseApp() {
    throw ExitException()
  }

  /** Exception thrown when the app is requested to terminate in tests. */
  class ExitException :
    RuntimeException("App termination requested")
}
