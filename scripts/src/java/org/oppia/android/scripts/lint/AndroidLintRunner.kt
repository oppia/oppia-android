package org.oppia.android.scripts.lint

import com.android.SdkConstants
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.lang.Module
import java.lang.ModuleLayer
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import com.android.tools.lint.Main as LintCli

/**
 * The main entrypoint to analyze the codebase for Android Lint issues.
 *
 * Usage:
 *   bazel run //scripts:android_lint_check -- <path_to_repository_root> [--group_by_severity] [--processTimeout=<minutes>]
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository (required)
 * - --group_by_severity: Optional flag to group issues by severity
 * - --processTimeout=<minutes>: Process timeout in minutes
 *
 * Examples:
 *   bazel run //scripts:android_lint_check -- $(pwd)
 *   bazel run //scripts:android_lint_check -- $(pwd) --group_by_severity
 *   bazel run //scripts:android_lint_check -- $(pwd) --processTimeout=15
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "<path_to_repository_root argument> is required: \$(pwd)"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) {
    "Repository root path does not exist: ${args[0]}"
  }

  val groupByIssueSeverity = args.contains("--group_by_severity")
  val processTimeout = args.find { it.startsWith("--processTimeout=") }
    ?.substringAfter("=")
    ?.toLongOrNull() ?: 10L

  val temporaryDir = Files.createTempDirectory("").parent.toFile()
  val workingDirectory = File(temporaryDir, "lint_analysis").apply { mkdirs() }

  println("Using ${workingDirectory.absolutePath} as an intermediary working directory")

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(
      scriptBgDispatcher,
      processTimeout = processTimeout,
      processTimeoutUnit = TimeUnit.MINUTES
    )

    val bazelClient = BazelClient(repoRoot, commandExecutor)
    val lintAnalyzer = AndroidLintAnalyzer(
      repoRoot = repoRoot,
      workingDirectory = workingDirectory,
      bazelClient = bazelClient,
      groupByIssueSeverity = groupByIssueSeverity
    )

    lintAnalyzer.runAnalysis()
  }
}

/**
 * Manages the Android Lint analysis process.
 *
 * @param repoRoot The root directory of the repository
 * @param workingDirectory The temporary working directory for lint analysis
 * @param bazelClient The Bazel client for executing Bazel commands
 * @param groupByIssueSeverity Whether to group issues by severity in the output
 */
class AndroidLintAnalyzer(
  private val repoRoot: File,
  private val workingDirectory: File,
  private val bazelClient: BazelClient,
  private val groupByIssueSeverity: Boolean = false
) {
  companion object {
    private const val LINT_REPORT_FILE = "lint-report.xml"
    private const val JAVA_HOME_KEY = "java-home"
    private const val JAVA_RUNTIME_KEY = "java-runtime"
  }

  private val reportFile = File(workingDirectory, LINT_REPORT_FILE)

  /** Runs the complete lint analysis process. */
  fun runAnalysis() {
    val projectDescriptionFile = generateProjectDescription()
    val lintRunner = AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile,
      groupByIssueSeverity = groupByIssueSeverity
    )

    val bazelInfo = bazelClient.retrieveBazelInfo()
    val javaConfig = JavaConfiguration(bazelInfo)
    val buildSdkVersion = AndroidBuildSdkProperties().buildSdkVersion
    val cliArgs = lintRunner.prepareLintArguments(
      jdkHome = javaConfig.getJdkHome(),
      javaVersion = javaConfig.getVersion(),
      buildSdkVersion = buildSdkVersion.toString()
    )

    lintRunner.runLint(cliArgs)
  }

  /** Generates the project description XML file. */
  private fun generateProjectDescription(): File {
    val lintProjectDescription = LintProjectDescription(
      repoRoot = repoRoot,
      workingDirectory = workingDirectory,
      bazelClient = bazelClient
    )
    return lintProjectDescription.generateProjectDescriptionXml()
  }

  /** Java configuration class. */
  private class JavaConfiguration(bazelInfo: Map<String, String>) {
    private val jdkHome: File
    private val version: String

    init {
      jdkHome = File(
        bazelInfo[JAVA_HOME_KEY] ?: error("$JAVA_HOME_KEY not found in bazel info output")
      )

      val javaRuntime = bazelInfo[JAVA_RUNTIME_KEY]
        ?: error("$JAVA_RUNTIME_KEY not found in bazel info output")

      val versionRegex = Regex("""build (\d+\.\d+\.\d+)""")
      version = versionRegex.find(javaRuntime)
        ?.groupValues?.get(1)
        ?: error("Could not extract Java version from: $javaRuntime")
    }

    fun getJdkHome(): File = jdkHome

    fun getVersion(): String = version
  }
}

/**
 * Runs the Android Lint tool and reports issues.
 *
 * @param reportFile The XML file where lint results will be written
 * @param projectDescriptionFile The XML file containing project configuration
 * @param groupByIssueSeverity Whether to group issues by severity in the output
 */
class AndroidLintRunner(
  private val reportFile: File,
  private val projectDescriptionFile: File,
  private val groupByIssueSeverity: Boolean = false
) {
  companion object {
    private const val LINT_CLIENT_ID = "cli"
    private const val KOTLIN_LANGUAGE_VERSION = "1.6"
    private const val JDK_RELEASE_FILE = "release"

    private const val SUCCESS = 0
    private const val ISSUES_FOUND = 1
    private const val INVALID_USAGE = 2
    private const val CANNOT_OVERWRITE = 3
    private const val HELP_INVOKED = 4
    private const val INVALID_ARGUMENT = 5

    private val ERROR_CODE_MESSAGES = mapOf(
      INVALID_USAGE to "Invalid usage of Lint command",
      CANNOT_OVERWRITE to "Cannot overwrite existing file",
      HELP_INVOKED to "Help command invoked",
      INVALID_ARGUMENT to "Invalid command-line argument"
    )
  }

  /**
   * Invokes the Lint CLI to perform analysis and prints the results.
   *
   * @param cliArgs The command-line arguments to pass to the Lint CLI
   */
  fun runLint(cliArgs: Array<String>) {
    val exitCode = LintCli().run(cliArgs)

    // Allow exit code 1 since it indicates issues with
    // severity Error which is being handled by LintAnalysisReporter.
    if (exitCode != SUCCESS && exitCode != ISSUES_FOUND) {
      val reason = ERROR_CODE_MESSAGES[exitCode] ?: "Unknown failure or internal error"
      error("Lint analysis failed with exit code $exitCode: $reason")
    }

    val reporter = LintAnalysisReporter()
    val issues = reporter.parseLintReport(reportFile.absolutePath)
    reporter.printLintReport(issues, groupByIssueSeverity)
  }

  /**
   * Prepares the command-line arguments for the Lint tool.
   *
   * @param jdkHome The JDK home directory
   * @param javaVersion The Java version to use for analysis
   * @return Array of command-line arguments for the Lint CLI
   */
  fun prepareLintArguments(
    jdkHome: File,
    javaVersion: String,
    buildSdkVersion: String
  ): Array<String> {
    prepareJdkEnvironment(jdkHome)

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
      "--compile-sdk-version", buildSdkVersion,
      "--kotlin-language-level", KOTLIN_LANGUAGE_VERSION,
      "--java-language-level", javaVersion,
      "--project", projectDescriptionFile.absolutePath,
      "--xml", reportFile.absolutePath
    )
  }

  /**
   * Prepares JDK environment for lint by creating a release file if needed.
   * Lint uses $JAVA_HOME/release, so we manually populate it if missing.
   */
  private fun prepareJdkEnvironment(jdkHome: File) {
    require(jdkHome.exists() && jdkHome.isDirectory) {
      "JDK home path does not exist or is not a directory: ${jdkHome.absolutePath}"
    }

    val releaseFile = File(jdkHome, JDK_RELEASE_FILE)
    if (!releaseFile.exists()) {
      try {
        val modulesString = generateModulesString()
        releaseFile.writeText(modulesString)
      } catch (e: Exception) {
        throw IllegalStateException(
          "Failed to prepare JDK release file: ${releaseFile.path}", e
        )
      }
    }
  }

  /** Generates the MODULES string for the JDK release file. */
  private fun generateModulesString(): String {
    return try {
      ModuleLayer.boot()
        .modules()
        .joinToString(
          separator = " ",
          prefix = "MODULES=\"",
          postfix = "\"",
          transform = Module::getName
        )
    } catch (e: Exception) {
      throw IllegalStateException("Failed to generate modules string from boot layer")
    }
  }

  /** Retrieves the Android SDK path from environment variables. */
  private fun getAndroidSdkPath(): String {
    return System.getenv(SdkConstants.ANDROID_HOME_ENV)
      ?: throw IllegalStateException(
        "ANDROID_HOME environment variable is not set. " +
          "Please set it to the path of your Android SDK."
      )
  }
}
