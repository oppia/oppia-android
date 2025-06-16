package org.oppia.android.scripts.lint

import java.io.File
import java.nio.file.Files
import com.android.tools.lint.Main as LintCli

/**
 * The main entrypoint to analyse the codebase for Android Lint issues.
 *
 * Usage:
 *   bazel run //scripts:android_lint_check -- <path_to_repository_root> [--group_by_severity]
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository (required)
 * - --group_by_severity: Optional flag to group issues by severity
 *
 * Examples:
 *    bazel run //scripts:android_lint_check -- $(pwd)
 *    bazel run //scripts:android_lint_check -- $(pwd) --group_by_severity
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
  val projectDescriptionFile = File(parentDestDir, "lint-project-description.xml")
  val lintRunner = AndroidLintRunner(
    reportFile = reportFile,
    projectDescriptionFile = projectDescriptionFile,
    groupByIssueSeverity = groupByIssueSeverity
  )
  val cliArgs = lintRunner.prepareLintArguments()

  lintRunner.runLint(cliArgs)
}

/** Runs the Android Lint tool and reports issues.
 *
 * @param reportFile the file where Lint results will be written
 * @param projectDescriptionFile the file containing the project description for Lint
 * @param groupByIssueSeverity whether to group issues by severity in the report
 */
class AndroidLintRunner(
  private val reportFile: File,
  private val projectDescriptionFile: File,
  private val groupByIssueSeverity: Boolean = false
) {
  companion object {
    private val ERROR_CODE_MESSAGES = mapOf(
      2 to "Invalid usage of Lint command.",
      3 to "Cannot overwrite existing file.",
      4 to "Help command invoked.",
      5 to "Invalid command-line argument.",
    )
  }
  init {
    require(projectDescriptionFile.exists()) {
      "Project description file does not exist: ${projectDescriptionFile.absolutePath}"
    }
    require(projectDescriptionFile.extension == "xml") {
      "Project description file must be an .xml file: ${projectDescriptionFile.name}"
    }
  }
  /**
   * Invokes the Lint CLI to perform analysis and prints the results.
   *
   * @param cliArgs arguments to pass to the Lint CLI
   */
  fun runLint(cliArgs: Array<String>) {

    // TODO(#5734): Implement the project description for Lint execution.
    val exitCode = LintCli().run(cliArgs) // Currently returns error code due to missing description

    // Allow exit code 1 since it indicates issues with
    // severity Error which is being handled by LintAnalysisReporter.
    check(exitCode == 0 || exitCode == 1) {
      val reason = ERROR_CODE_MESSAGES[exitCode]
        ?: "Unknown failure or internal error."
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
