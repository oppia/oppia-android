package org.oppia.android.scripts.lint

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import com.android.tools.lint.Main as LintCli

/**
 * The main entrypoint to analyse the codebase for Android Lint issues.
 *
 * Usage:
 *   bazel run //scripts:android_lint_check -- <path_to_repository_root> [--group_by_severity]
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository (required)
 * - group_by_severity: Optional flag to group issues by severity
 * - processTimeout: The amount of time that should be waited before considering a process as 'hung',
 *    in minutes.
 *
 * Examples:
 *    bazel run //scripts:android_lint_check -- $(pwd)
 *    bazel run //scripts:android_lint_check -- $(pwd) --group_by_severity
 *    bazel run //scripts:android_lint_check -- $(pwd) --processTimeout=10
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "<path_to_repository_root argument> is required: \$(pwd)"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val groupByIssueSeverity = args.contains("--group_by_severity")
  val temporaryDir = Files.createTempDirectory("").parent.toFile()
  val parentDestDir = File(temporaryDir, "lint_analysis").apply { mkdirs() }
  println("Using ${parentDestDir.absolutePath} as an intermediary working directory")

  val reportFile = File(parentDestDir, "lint-report.xml")
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("--processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5
    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )
    val bazelClient by lazy { BazelClient(repoRoot, commandExecutor) }
    val lintProjectDescription = LintProjectDescription(
      repoRoot = repoRoot,
      workingDirectory = parentDestDir,
      bazelClient = bazelClient,
    )
    val lintRunner = AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = lintProjectDescription.generateProjectDescriptionXml(),
      groupByIssueSeverity = groupByIssueSeverity,
    )
    val cliArgs = lintRunner.prepareLintArguments()

    lintRunner.runLint(cliArgs)
  }
}

/** Runs the Android Lint tool and reports issues. */
class AndroidLintRunner(
  private val reportFile: File,
  private val projectDescriptionFile: File,
  private val groupByIssueSeverity: Boolean = false
) {

  /**
   * Invokes the Lint CLI to perform analysis and prints the results.
   *
   * @param cliArgs arguments to pass to the Lint CLI
   */
  fun runLint(cliArgs: Array<String>) {

    val exitCode = LintCli().run(cliArgs)
    check(exitCode == 0 || exitCode == 1) {
      val reason = when (exitCode) {
        2 -> "Invalid usage of Lint command."
        3 -> "Cannot overwrite existing file."
        4 -> "Help command invoked."
        5 -> "Invalid command-line argument."
        else -> "Unknown failure or internal error."
      }
      "Lint analysis failed with exit code $exitCode: $reason"
    }
    val reporter = LintAnalysisReporter()
    val issues = reporter.parseLintReport(reportFile.absolutePath)
    reporter.printLintReport(
      issues,
      groupByIssueSeverity,
    )
  }

  /**
   * Prepares the command-line arguments for the Lint tool.
   *
   * @return array of arguments to be passed to Lint
   */
  fun prepareLintArguments(): Array<String> = arrayOf(
    "-Wall",
    "--quiet",
    "--fullpath",
    "--showall",
    "--exitcode",
    "--offline",
    "--project", projectDescriptionFile.absolutePath,
    "--xml", reportFile.absolutePath
  )
}
