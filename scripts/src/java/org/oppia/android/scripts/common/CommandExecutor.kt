package org.oppia.android.scripts.common

import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Deferred

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
  fun executeCommandInForeground(
    command: String,
    vararg arguments: String,
    stdoutRedirection: OutputRedirectionStrategy = OutputRedirectionStrategy.TRACK_AS_OUTPUT,
    stderrRedirection: OutputRedirectionStrategy = OutputRedirectionStrategy.TRACK_AS_ERROR
  ): CommandResult

  fun executeCommandInBackgroundAsync(
    command: String, vararg arguments: String
  ): Deferred<CommandResult>

  // TODO: Update docs for executeCommandInForeground and CommandResult.
  enum class OutputRedirectionStrategy {
    DROP,
    TRACK_AS_OUTPUT,
    TRACK_AS_ERROR,
    REDIRECT_TO_PARENT_STREAM
  }

  interface Builder {
    fun setEnvironmentVariable(name: String, value: String): Builder

    fun setProcessTimeout(
      timeout: Long = WAIT_PROCESS_TIMEOUT_MS, timeoutUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): Builder

    fun create(workingDirectory: File): CommandExecutor

    interface Factory {
      fun createBuilder(): Builder
    }
  }

  companion object {
    /**
     * The default amount of time that should be waited before considering a process as 'hung', in
     * milliseconds.
     */
    const val WAIT_PROCESS_TIMEOUT_MS = 60_000L
  }
}
