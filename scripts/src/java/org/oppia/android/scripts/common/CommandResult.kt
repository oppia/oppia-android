package org.oppia.android.scripts.common

/** The result of executing a command using [CommandExecutorImpl.executeCommandInForeground]. */
data class CommandResult(
  /** The exit code of the application. */
  val exitCode: Int,
  /** The lines of output from the command, including both error & standard output lines. */
  val output: List<String>,
  /** The lines of error output, or empty if error output is redirected to [output]. */
  val errorOutput: List<String>,
  /** The fully-formed command line executed by the application to achieve this result. */
  val command: List<String>,
) {
  val commandLine: String
    get() = command.joinToString(separator = " ")

  val outputLines: String
    get() = output.joinToString(separator = "\n")

  val errorLines: String
    get() = errorOutput.joinToString(separator = "\n")
}
