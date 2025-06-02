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
    "Usage: bazel run //scripts:android_lint_check -- <path_to_repository_root>"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val lintRunner = AndroidLintRunner()
  lintRunner.runLint()
}

/** Runs the Android lint tool to generate reports. */
class AndroidLintRunner {

  /** Invokes the tool to run Lint analysis. */
  fun runLint() {

    val parentDestDir = Files.createTempDirectory("lint_analysis_").toFile()
    println("Using ${parentDestDir.absolutePath} as an intermediary working directory")
    val reportFile = File(parentDestDir, "lint-report.xml")
    val projectDescriptionFile = File(parentDestDir, "lint-project-description.xml")
    val cliArgs = prepareLintArguments(reportFile.absolutePath, projectDescriptionFile.absolutePath)

    LintCli().run(cliArgs)
  }

  /**
   * Prepares the command line arguments for the Android Lint tool.
   *
   * @param reportPath path to the XML report file
   * @return array of command line arguments
   */
  private fun prepareLintArguments(
    reportPath: String,
    projectDescriptionPath: String
  ): Array<String> {

    return arrayOf(
      "-Wall",
      "--quiet",
      "--fullpath",
      "--showall",
      "--exitcode",
      "--offline",
      "--project", projectDescriptionPath,
      "--xml", reportPath,
    )
  }
}
