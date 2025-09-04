package org.oppia.android.scripts.lint

import com.android.SdkConstants
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.lang.Module
import java.lang.ModuleLayer
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import com.android.tools.lint.Main as LintCli

/** The default timeout duration for executing external processes. */
private const val DEFAULT_PROCESS_TIMEOUT_MINUTES = 10L
/** Default path to the exemption .pb file. */
private const val DEFAULT_PROTO_BINARY_PATH = "scripts/assets/android_lint_exemptions.pb"

/**
 * The main entrypoint to analyze the codebase for Android Lint issues.
 *
 * Usage:
 *   bazel run //scripts:android_lint_check -- <path_to_repository_root>
 *   [--proto=<path_to_proto_binary>] [--group_by_severity] [--processTimeout=<minutes>]
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository (required)
 * - --proto=<path_to_proto_binary>: Relative path to the exemption .pb file.
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
  val exemptionProtoPath = args.find { it.startsWith("--proto=") }?.let { option ->
    val path = option.substringAfter("=")
    require(path.endsWith(".pb")) {
      "Invalid exemption file: $path. The file must have a .pb extension."
    }
    path
  } ?: DEFAULT_PROTO_BINARY_PATH
  val groupByIssueSeverity = args.contains("--group_by_severity")
  val processTimeout = args.find { it.startsWith("--processTimeout=") }
    ?.substringAfter("=")
    ?.toLongOrNull() ?: DEFAULT_PROCESS_TIMEOUT_MINUTES

  val temporaryDir = Files.createTempDirectory("").parent.toFile()
  val workingDirectory = File(temporaryDir, "lint_analysis").apply { mkdirs() }

  println("Using ${workingDirectory.absolutePath} as an intermediary working directory")

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(
      scriptBgDispatcher,
      processTimeout = processTimeout,
      processTimeoutUnit = TimeUnit.MINUTES
    )

    val lintAnalyzer = AndroidLintAnalyzer(
      repoRoot = repoRoot,
      workingDirectory = workingDirectory,
      commandExecutor = commandExecutor,
      exemptionProtoPath = exemptionProtoPath,
      groupByIssueSeverity = groupByIssueSeverity
    )

    lintAnalyzer.runAnalysis()
  }
}

/**
 * Manages the Android Lint analysis process.
 *
 * @param repoRoot the root directory of the repository
 * @param workingDirectory the temporary working directory for lint analysis
 * @param commandExecutor executes the specified command in the specified working directory
 * @param groupByIssueSeverity whether to group issues by severity in the output
 */
class AndroidLintAnalyzer(
  private val repoRoot: File,
  private val workingDirectory: File,
  private val commandExecutor: CommandExecutor,
  private val exemptionProtoPath: String = DEFAULT_PROTO_BINARY_PATH,
  private val groupByIssueSeverity: Boolean = false,
  private val reportUnusedEnum: Boolean = true
) {
  private val bazelClient = BazelClient(repoRoot, commandExecutor)
  companion object {
    private const val LINT_REPORT_FILE = "lint-report.xml"
  }

  private val reportFile = File(workingDirectory, LINT_REPORT_FILE)

  /** Runs the complete lint analysis process. */
  fun runAnalysis() {
    val projectDescriptionFile = generateProjectDescription()
    val lintRunner = AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile,
      repoRoot = repoRoot,
      exemptionProtoPath = exemptionProtoPath,
      groupByIssueSeverity = groupByIssueSeverity,
      reportUnusedEnum = reportUnusedEnum
    )
    val sdkProperties = AndroidBuildSdkProperties()
    val bazelInfo = bazelClient.retrieveBazelInfo()
    val javaConfig = JavaConfiguration(bazelInfo)
    val buildSdkVersion = sdkProperties.buildSdkVersion
    val kotlinVersion = sdkProperties.kotlinCompilerVersion
    val cliArgs = lintRunner.prepareLintArguments(
      jdkHome = javaConfig.getJdkHome(),
      javaVersion = javaConfig.getVersion(),
      buildSdkVersion = buildSdkVersion.toString(),
      kotlinCompilerVersion = extractKotlinMajorVersion(kotlinVersion)
    )

    lintRunner.runLint(cliArgs)
  }

  /** Generates the project description XML file. */
  private fun generateProjectDescription(): File {
    val lintProjectDescription = LintProjectDescription(
      repoRoot = repoRoot,
      workingDirectory = workingDirectory,
      commandExecutor = commandExecutor
    )
    return lintProjectDescription.generateProjectDescriptionXml()
  }

  private fun extractKotlinMajorVersion(version: String): String {
    val cleanedVersion = version.substringBefore("-")
    val parts = cleanedVersion.split(".")
    return listOfNotNull(
      parts.getOrNull(0),
      parts.getOrNull(1)
    ).joinToString(".")
  }
}

/**
 * Runs the Android Lint tool and reports issues.
 *
 * @param reportFile the XML file where lint results will be written
 * @param projectDescriptionFile the XML file containing project configuration
 * @param groupByIssueSeverity whether to group issues by severity in the output
 * @param reportUnusedEnum whether to report unused exemptions in the output
 */
class AndroidLintRunner(
  private val reportFile: File,
  private val projectDescriptionFile: File,
  private val repoRoot: File,
  private val exemptionProtoPath: String = DEFAULT_PROTO_BINARY_PATH,
  private val groupByIssueSeverity: Boolean = false,
  private val reportUnusedEnum: Boolean = true
) {
  companion object {
    private const val LINT_CLIENT_ID = "cli"
    private const val JDK_RELEASE_FILE = "release"

    private const val SUCCESS = 0
    private const val ISSUES_FOUND = 1
    private const val INVALID_USAGE = 2
    private const val CANNOT_OVERWRITE = 3
    private const val HELP_INVOKED = 4
    private const val INVALID_ARGUMENT = 5

    private val ERROR_CODE_MESSAGES = mapOf(
      INVALID_USAGE to "Invalid usage of Lint command.",
      CANNOT_OVERWRITE to "Cannot overwrite existing file.",
      HELP_INVOKED to "Help command invoked.",
      INVALID_ARGUMENT to "Invalid command-line argument."
    )
  }

  /**
   * Invokes the Lint CLI to perform analysis and prints the results.
   *
   * @param cliArgs the command-line arguments to pass to the Lint CLI
   */
  fun runLint(cliArgs: Array<String>) {
    val exitCode = LintCli().run(cliArgs)

    // Allow exit code 1(ISSUES_FOUND) since it indicates issues with
    // severity Error which is being handled by LintAnalysisReporter.
    if (exitCode != SUCCESS && exitCode != ISSUES_FOUND) {
      val reason = ERROR_CODE_MESSAGES[exitCode] ?: "Unknown failure or internal error"
      error("Lint analysis failed with exit code $exitCode: $reason")
    }

    reportLintIssues()
  }

  /**
   * Prepares the command-line arguments for the Lint tool.
   *
   * @param jdkHome the JDK home directory
   * @param javaVersion the Java version to use for analysis
   * @return array of command-line arguments for the Lint CLI
   */
  fun prepareLintArguments(
    jdkHome: File,
    javaVersion: String,
    buildSdkVersion: String,
    kotlinCompilerVersion: String
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
      "--kotlin-language-level", kotlinCompilerVersion,
      "--java-language-level", javaVersion,
      "--project", projectDescriptionFile.absolutePath,
      "--xml", reportFile.absolutePath
    )
  }

  private fun reportLintIssues() {
    val reporter = LintAnalysisReporter(repoRoot)
    val allIssues = reporter.parseLintReport(reportFile.absolutePath)

    require(File(exemptionProtoPath).exists()) {
      "Exemption file does not exist: $exemptionProtoPath"
    }

    val exemptions = reporter.loadExemptionsProto(exemptionProtoPath)

    val filteredIssues = reporter.filterExemptedIssues(
      issues = allIssues,
      exemptions = exemptions.androidLintExemptionList
    )

    val redundantExemptions = reporter.findRedundantExemptions(
      issues = allIssues,
      exemptions = exemptions.androidLintExemptionList
    )

    reporter.printLintReport(
      filteredIssues = filteredIssues,
      groupByIssueSeverity = groupByIssueSeverity,
      redundantExemptions = redundantExemptions,
      reportUnusedEnum = reportUnusedEnum,
      allIssues = allIssues
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
