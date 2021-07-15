package org.oppia.android.scripts.common

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The default amount of time that should be waited before considering a process as 'hung', in
 * milliseconds.
 */
const val WAIT_PROCESS_TIMEOUT_MS = 6_000_000L

/** Default implementation of [CommandExecutor]. */
class CommandExecutorImpl(
  private val processTimeout: Long = WAIT_PROCESS_TIMEOUT_MS,
  private val processTimeoutUnit: TimeUnit = TimeUnit.MILLISECONDS
) : CommandExecutor {
  override fun executeCommand(
    workingDir: File,
    command: String,
    vararg arguments: String,
    includeErrorOutput: Boolean
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
