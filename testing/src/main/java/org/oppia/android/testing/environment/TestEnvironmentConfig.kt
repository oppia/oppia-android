package org.oppia.android.testing.environment

import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/** Utility class that provides details on the local test environment configuration. */
class TestEnvironmentConfig @Inject constructor(
  private val machineLocale: OppiaLocale.MachineLocale
) {
  /** Returns whether the current runtime environment is being run with Bazel. */
  fun isUsingBazel(): Boolean {
    // Some of the system properties are Bazel-specific; this is an easy hacky way to check if any
    // of them are set to indicate a Bazel environment.
    return System.getProperties().keys().asSequence().map {
      machineLocale.run { it.toString().toMachineLowerCase() }
    }.any { "bazel" in it }
  }
}
