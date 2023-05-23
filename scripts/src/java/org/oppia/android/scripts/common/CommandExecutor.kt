package org.oppia.android.scripts.common

import java.io.File

/** Utility class for executing commands on the local filesystem. */
interface CommandExecutor {
  /**
   * Executes the specified [command] in the specified working directory [workingDir] with the
   * provided arguments being passed as arguments to the command.
   *
   * Any exceptions thrown when trying to execute the application will be thrown by this method.
   * Any failures in the underlying process should not result in an exception.
   *
   * @param workingDir the working directory from which the run the command
   * @param command the specific application to run
   * @param arguments zero or more arguments to pass to the application
   * @param includeErrorOutput whether to include error output in the returned result's
   *     [CommandResult.output], otherwise it's only represented in [CommandResult.errorLines]. This
   *     defaults to true.
   * @param standardOutputMonitor a monitor which receives each completed line printed to standard
   *     output, as it's received. This only includes error lines if [includeErrorOutput] is true.
   *     This defaults to an empty lambda which no-ops.
   * @param standardErrorMonitor a monitor which receives each completed line printed to the new
   *     process's standard error output, as it's received. This only includes lines if
   *     [includeErrorOutput] is false, otherwise it's never used. This defaults to an empty lambda
   *     which no-ops.
   * @return a [CommandResult] that includes the error code & application output
   */
  fun executeCommand(
    workingDir: File,
    command: String,
    vararg arguments: String,
    includeErrorOutput: Boolean = true,
    standardOutputMonitor: (String) -> Unit = {},
    standardErrorMonitor: (String) -> Unit = {}
  ): CommandResult
}
