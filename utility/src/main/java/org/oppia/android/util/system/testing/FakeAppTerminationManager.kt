package org.oppia.android.util.system.testing

import javax.inject.Inject
import org.oppia.android.util.system.AppTerminationManager

/**
 * Fake implementation of [AppTerminationManager] that throws an exception instead of terminating
 * the process. This allows tests to verify that app termination was requested without actually
 * killing the test process, and enables JaCoCo to properly record coverage.
 */
class FakeAppTerminationManager @Inject constructor() : AppTerminationManager {
  override fun forceCloseApp() {
    throw Exception("App termination requested")
  }
}
