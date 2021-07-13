package org.oppia.android.scripts.maven

import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.concurrent.TimeUnit

private const val MAVEN_PREFIX_LENGTH = 9
private const val WAIT_PROCESS_TIMEOUT_MS = 60_000L

private const val LICENSES_TAG = "<licenses>"
private const val LICENSES_CLOSE_TAG = "</licenses>"
private const val LICENSE_TAG = "<license>"
private const val NAME_TAG = "<name>"
private const val URL_TAG = "<url>"

class UtilityProviderImpl(): UtilityProvider {

  override fun scrapeText(link: String): String {
    return URL(link).openStream().bufferedReader().readText()
  }

  override fun runBazelQueryCommand(
    rootPath: String,
    vararg args: String
  ): List<String> {
    val rootDirectory = File(rootPath).absoluteFile
    val bazelClient = BazelClient(rootDirectory)
    val bazelQueryDepsNames = mutableListOf<String>()
    return bazelClient.executeBazelCommand(
      "query",
      "\'deps(deps(//:oppia)",
      "intersect",
      "//third_party/...)",
      "intersect",
      "@maven//...\'"
    )
  }

  private class BazelClient(private val rootDirectory: File) {
    fun executeBazelCommand(
      vararg arguments: String,
      allowPartialFailures: Boolean = false
    ): List<String> {
      val result =
        executeCommand(rootDirectory, command = "bazel", *arguments, includeErrorOutput = false)
      // Per https://docs.bazel.build/versions/main/guide.html#what-exit-code-will-i-get error code of
      // 3 is expected for queries since it indicates that some of the arguments don't correspond to
      // valid targets. Note that this COULD result in legitimate issues being ignored, but it's
      // unlikely.
      val expectedExitCodes = if (allowPartialFailures) listOf(0, 3) else listOf(0)
      check(result.exitCode in expectedExitCodes) {
        "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
          "\nStandard output:\n${result.output.joinToString("\n")}" +
          "\nError output:\n${result.errorOutput.joinToString("\n")}"
      }
      return result.output
    }

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
    private fun executeCommand(
      workingDir: File,
      command: String,
      vararg arguments: String,
      includeErrorOutput: Boolean = true
    ): CommandResult {
      check(workingDir.isDirectory) {
        "Expected working directory to be an actual directory: $workingDir"
      }
      val assembledCommand = listOf(command) + arguments.toList()
      println(assembledCommand)
      val command = assembledCommand.joinToString(" ")
      val process = ProcessBuilder()
        .command("bash", "-c", command)
        .directory(workingDir)
        .redirectErrorStream(includeErrorOutput)
        .start()
      val finished = process.waitFor(WAIT_PROCESS_TIMEOUT_MS, TimeUnit.MILLISECONDS)
      check(finished) { "Process did not finish within the expected timeout" }
      return CommandResult(
        process.exitValue(),
        process.inputStream.bufferedReader().readLines(),
        if (!includeErrorOutput) process.errorStream.bufferedReader().readLines() else listOf(),
        assembledCommand,
      )
    }
  }

  /** The result of executing a command using [executeCommand]. */
  private data class CommandResult(
    /** The exit code of the application. */
    val exitCode: Int,
    /** The lines of output from the command, including both error & standard output lines. */
    val output: List<String>,
    /** The lines of error output, or empty if error output is redirected to [output]. */
    val errorOutput: List<String>,
    /** The fully-formed command line executed by the application to achieve this result. */
    val command: List<String>,
  )
}