package org.oppia.android.scripts.common

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The default amount of time that should be waited before considering a process as 'hung', in
 * milliseconds.
 */
const val WAIT_PROCESS_TIMEOUT_MS = 60_000L

/** Utility class for executing commands on the local filesystem. */
class CommandExecutor(
  private val processTimeout: Long = WAIT_PROCESS_TIMEOUT_MS,
  private val processTimeoutUnit: TimeUnit = TimeUnit.MILLISECONDS
) {
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
  ): CommandResult {
    check(workingDir.isDirectory) {
      "Expected working directory to be an actual directory: $workingDir"
    }
    val assembledCommand = listOf(command) + arguments.toList()
    val process =
      ProcessBuilder(assembledCommand)
        .directory(workingDir)
        .redirectErrorStream(includeErrorOutput)
        .start()
    val finished = process.waitFor(processTimeout, processTimeoutUnit)
    check(finished) { "Process did not finish within the expected timeout" }
    return CommandResult(
      process.exitValue(),
      process.inputStream.bufferedReader().readLines(),
      if (!includeErrorOutput) process.errorStream.bufferedReader().readLines() else listOf(),
      assembledCommand,
    )
  }
}
