package org.oppia.android.scripts.lint

import java.io.File
import java.nio.file.Files
import com.android.tools.lint.Main as LintCli

/**
 * The main entrypoint to analyse the codebase for Android Lint issues.
 *
 * Usage:
 *   bazel run //scripts:android_lint_check -- <path_to_repository_root>
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository.
 *
 * Example:
 *    bazel run //scripts:android_lint_check -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "<path_to_repository_root argument> is required: \$(pwd)"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val temporaryDir = Files.createTempDirectory("").parent.toFile()
  val parentDestDir = File(temporaryDir, "lint_analysis").apply { mkdirs() }
  println("Using ${parentDestDir.absolutePath} as an intermediary working directory")

  val reportFile = File(parentDestDir, "lint-report.xml")
  val projectDescriptionFile = File(parentDestDir, "lint-project-description.xml")
  val lintRunner = AndroidLintRunner(
    reportPath = reportFile.absolutePath,
    projectDescriptionPath = projectDescriptionFile.absolutePath
  )
  val cliArgs = lintRunner.prepareLintArguments()

  lintRunner.runLint(cliArgs)
}

/** Runs the Android Lint tool and reports issues. */
class AndroidLintRunner(
  private val reportPath: String,
  private val projectDescriptionPath: String
) {

  /**
   * Invokes the Lint CLI to perform analysis and prints the results.
   *
   * @param cliArgs arguments to pass to the Lint CLI
   */
  fun runLint(cliArgs: Array<String>) {

    // TODO(#5734): Implement the project description for Lint execution.
    val exitCode = LintCli().run(cliArgs) // Currently returns error code due to missing description
    check(exitCode == 0) {
      val reason = when (exitCode) {
        1 -> "Lint errors detected."
        2 -> "Invalid usage of Lint command."
        3 -> "Cannot overwrite existing file."
        4 -> "Help command invoked."
        5 -> "Invalid command-line argument."
        else -> "Unknown failure or internal error."
      }
      "Lint analysis failed with exit code $exitCode: $reason"
    }
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
    "--project", projectDescriptionPath,
    "--xml", reportPath
  )
}
