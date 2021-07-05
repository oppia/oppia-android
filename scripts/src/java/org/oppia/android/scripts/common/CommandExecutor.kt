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
   * @param includeErrorOutput whether to include error output in the returned [CommandResult],
   *     otherwise it's discarded
   * @return a [CommandResult] that includes the error code & application output
   */
  fun executeCommand(
    workingDir: File,
    command: String,
    vararg arguments: String,
    includeErrorOutput: Boolean = true
  ): CommandResult
}
