package org.oppia.android.scripts.lint

import com.android.SdkConstants
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
      ?.toLongOrNull() ?: 10
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
    val jdkHome = File(
      bazelClient.retrieveBazelInfo()["java-home"]
        ?: error("java-home not found in bazel info output")
    )
    val cliArgs = lintRunner.prepareLintArguments(
      repoRoot = repoRoot,
      jdkHome = jdkHome
    )

    lintRunner.runLint(cliArgs)
  }
}

/** Runs the Android Lint tool and reports issues. */
class AndroidLintRunner(
  private val reportFile: File,
  private val projectDescriptionFile: File,
  private val groupByIssueSeverity: Boolean = false
) {
  companion object {
    private const val LINT_CLIENT_ID = "cli"
    private const val KOTLIN_LANGUAGE_VERSION = "1.6"
    private const val JAVA_VERSION = "11"
    private const val BUILD_VARS_FILE = "build_vars.bzl"
  }
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
  fun prepareLintArguments(
    repoRoot: File,
    jdkHome: File
  ): Array<String> {
    val buildVarsFile = File(repoRoot, BUILD_VARS_FILE)
    return arrayOf(
      "-Wall",
      "--quiet",
      "--fullpath",
      "--showall",
      "--exitcode",
      "--offline",
      "--client-id", LINT_CLIENT_ID,
      "--jdk-home", jdkHome.absolutePath,
      "--sdk-home", getAndroidSdkPath(),
      "--compile-sdk-version", getBuildSdkVersion(buildVarsFile),
      "--kotlin-language-level", KOTLIN_LANGUAGE_VERSION,
      "--java-language-level", JAVA_VERSION,
      "--project", projectDescriptionFile.absolutePath,
      "--xml", reportFile.absolutePath,
      "--html", "/home/manas-yu/lint-report.html",
    )
  }

  private fun getBuildSdkVersion(buildVarsFile: File): String {
    require(buildVarsFile.exists()) { "File not found: ${buildVarsFile.absolutePath}" }

    val compileSdkLine = buildVarsFile.readLines()
      .map { it.trim() }
      .firstOrNull { it.startsWith("BUILD_SDK_VERSION") }
      ?: error("BUILD_SDK_VERSION not found in file: ${buildVarsFile.absolutePath}")

    val value = compileSdkLine.substringAfter("=").trim().removeSurrounding("\"")
    require(value.isNotEmpty()) {
      "BUILD_SDK_VERSION value is empty in file: ${buildVarsFile.absolutePath}"
    }

    return value
  }

  private fun getAndroidSdkPath(): String {
    return System.getenv(SdkConstants.ANDROID_HOME_ENV)
      ?: throw IllegalStateException(
        "ANDROID_HOME environment variable is not set. " +
          "Please set it to the path of your Android SDK."
      )
  }
}
