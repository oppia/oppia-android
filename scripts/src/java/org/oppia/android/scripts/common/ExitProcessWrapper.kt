package org.oppia.android.scripts.common

/**
 * Wrapper interface for process exit operations in scripts.
 *
 * This allows for better code coverage by isolating the process exit call to a single
 * implementation that can be exempted from coverage requirements, and enables test-time swapping
 * of implementations.
 */
interface ExitProcessWrapper {
  /** Force closes the script with the specified [exitCode]. */
  fun forceCloseScript(exitCode: Int): Nothing
}
