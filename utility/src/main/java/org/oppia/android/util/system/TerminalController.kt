package org.oppia.android.util.system

/** Controller for terminal operations like exiting the process. */
interface TerminalController {
  /** Exits the current process with the specified [exitCode]. */
  fun exitProcess(exitCode: Int)
}
