package org.oppia.android.scripts.common

import kotlin.system.exitProcess

/**
 * Wrapper for [exitProcess] to be used in scripts.
 *
 * This allows for better code coverage by isolating the process exit call to a single file that can
 * be exempted from coverage requirements.
 */
class ExitProcessWrapper {
  /** Exits the process with the specified [exitCode]. */
  fun exitProcess(exitCode: Int): Nothing {
    kotlin.system.exitProcess(exitCode)
  }
}
