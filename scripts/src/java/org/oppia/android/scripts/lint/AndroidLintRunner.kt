package org.oppia.android.scripts.lint

import com.android.SdkConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.lang.Module
import java.lang.ModuleLayer
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import com.android.tools.lint.Main as LintCli

/** The default timeout duration for executing external processes. */
private const val DEFAULT_PROCESS_TIMEOUT_MINUTES = 15L
/** Default path to the exemption .pb file. */
private const val DEFAULT_PROTO_BINARY_PATH = "scripts/assets/android_lint_exemptions.pb"

/** Elapsed time displayer that shows running time of the script. */
class ElapsedTimeDisplayer(
  private val coroutineScope: CoroutineScope,
  private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
  private var startTime: Long = 0L
  private var timerJob: Job? = null

  @Volatile
  private var isTimerRunning = false

  @Volatile
  private var needsLineClear = false

  /** Starts the elapsed time display timer. */
  fun start() {
    if (isTimerRunning) return

    startTime = timeProvider()
    timerJob = coroutineScope.launch {
      isTimerRunning = true
      try {
        while (isActive) {
          displayElapsedTime()
          delay(1000)
        }
      } finally {
        isTimerRunning = false
      }
    }
  }

  /** Clears the timer line if it's currently displayed, preparing console for new output. */
  fun clearLine() {
    if (needsLineClear) {
      print("\r\u001B[K")
      System.out.flush()
      needsLineClear = false
    }
  }

  /** Stops the timer and returns the total elapsed time in milliseconds. */
  fun stop(): Long {
    val totalTime = timeProvider() - startTime
    timerJob?.cancel()
    timerJob = null

    clearLine()

    return totalTime
  }

  /** Displays the current elapsed time, overwriting the previous display. */
  private fun displayElapsedTime() {
    val elapsed = timeProvider() - startTime
    val formattedTime = formatDuration(elapsed)

    print("\rElapsed time: $formattedTime")
    System.out.flush()
    needsLineClear = true
  }

  /** Formats duration in milliseconds to HH:MM:SS format. */
  private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
  }
}

/** Extension function to format total execution time consistently. */
private fun Long.toFormattedDuration(): String {
  val totalSeconds = this / 1000
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60

  return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/** Execution modes for the lint script. */
enum class LintMode {
  /** Runs checks on only changed files, skipping project-scoped checks. */
  FAST,
  /** Runs the entire suite of checks with the full project description. */
  FULL,
  /** Lists all available lint checks and exits. */
  LIST_CHECKS,
  /** Compares LintCheckCatalog against the linter's actual check list. */
  CHECK_SCRIPT_CONSISTENCY;

  companion object {
    /** Parses a mode string (from --mode=) into a [LintMode], or null if invalid. */
    fun fromString(value: String): LintMode? = when (value) {
      "fast" -> FAST
      "full" -> FULL
      "list-checks" -> LIST_CHECKS
      "check-script-consistency" -> CHECK_SCRIPT_CONSISTENCY
      else -> null
    }

    /** Returns all valid mode strings for display in error messages. */
    fun validModeNames(): List<String> =
      listOf("fast", "full", "list-checks", "check-script-consistency")
  }
}

/**
 * The main entrypoint to analyze the codebase for Android Lint issues.
 *
 * Usage:
 *   bazel run //scripts:android_lint_check -- <path_to_repository_root>
 *   [--mode=<fast|full|list-checks|check-script-consistency>]
 *   [--proto=<path_to_proto_binary>] [--group_by_severity] [--processTimeout=<minutes>]
 *   [--timer] [--checks=<check1,check2>]
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository (required)
 * - --mode=<mode>: Execution mode (default: full)
 *   - fast: Runs checks that don't need full source context. Uses git diff to detect
 *     changed files and skips project-scoped checks. Best for local pre-push validation.
 *   - full: Runs the entire suite of checks with the full project description.
 *     This is what CI should use.
 *   - list-checks: Lists all available lint checks and exits.
 *   - check-script-consistency: Compares the checks cataloged in LintCheckCatalog
 *     against the linter's actual check list. Fails if any are missing or unrecognized.
 * - --proto=<path_to_proto_binary>: Relative path to the exemption .pb file.
 * - --group_by_severity: Optional flag to group issues by severity
 * - --processTimeout=<minutes>: Process timeout in minutes
 * - --timer: Optional flag to display elapsed time during execution
 * - --checks=<ids>: Run only the specified comma-separated check IDs
 *
 * Examples:
 *   bazel run //scripts:android_lint_check -- $(pwd)
 *   bazel run //scripts:android_lint_check -- $(pwd) --mode=fast --timer
 *   bazel run //scripts:android_lint_check -- $(pwd) --mode=list-checks
 *   bazel run //scripts:android_lint_check -- $(pwd) --checks=HardcodedText
 */
fun main(vararg args: String) {
  var exitCode = 0
  try {
    require(args.isNotEmpty()) {
      "<path_to_repository_root argument> is required: \$(pwd)"
    }

    val repoRoot = File(args[0])
    require(repoRoot.exists()) {
      "Repository root path does not exist: ${args[0]}"
    }

    val modeStr = args.find { it.startsWith("--mode=") }
      ?.substringAfter("=") ?: "full"
    val mode = LintMode.fromString(modeStr)
      ?: error(
        "Invalid mode: $modeStr. Valid modes: ${LintMode.validModeNames().joinToString(", ")}"
      )

    val processTimeout = args.find { it.startsWith("--processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: DEFAULT_PROCESS_TIMEOUT_MINUTES

    ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
      val commandExecutor = CommandExecutorImpl(
        scriptBgDispatcher,
        processTimeout = processTimeout,
        processTimeoutUnit = TimeUnit.MINUTES
      )

      val exemptionProtoPath = args.find { it.startsWith("--proto=") }?.let { option ->
        val path = option.substringAfter("=")
        require(path.endsWith(".pb")) {
          "Invalid exemption file: $path. The file must have a .pb extension."
        }
        path
      } ?: DEFAULT_PROTO_BINARY_PATH

      val checks = args.find { it.startsWith("--checks=") }
        ?.substringAfter("=")
        ?.split(",")
        ?: emptyList()

      val orchestrator = LintOrchestrator(
        repoRoot = repoRoot,
        commandExecutor = commandExecutor,
        scriptBgDispatcher = scriptBgDispatcher,
        exemptionProtoPath = exemptionProtoPath,
        groupByIssueSeverity = args.contains("--group_by_severity"),
        showTimer = args.contains("--timer"),
        explicitChecks = checks
      )

      orchestrator.execute(mode)
    }
  } catch (e: Exception) {
    e.printStackTrace()
    exitCode = 1
  } finally {
    exitProcess(exitCode)
  }
}

/**
 * Orchestrates lint execution across all modes.
 *
 * This class encapsulates the mode-specific logic that was previously in
 * `executeAndroidLintAnalysis()` and `runCheckScriptConsistency()`, making it
 * testable via constructor-injected [CommandExecutor].
 *
 * @param repoRoot the root directory of the repository
 * @param commandExecutor executes external commands (git, bazel, etc.)
 * @param scriptBgDispatcher coroutine dispatcher for background operations
 * @param exemptionProtoPath path to the lint exemptions .pb file
 * @param groupByIssueSeverity whether to group issues by severity in output
 * @param showTimer whether to display elapsed time during execution
 * @param explicitChecks optional list of specific check IDs to run
 */
class LintOrchestrator(
  private val repoRoot: File,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val exemptionProtoPath: String = DEFAULT_PROTO_BINARY_PATH,
  private val groupByIssueSeverity: Boolean = false,
  private val showTimer: Boolean = false,
  private val explicitChecks: List<String> = emptyList()
) {

  /**
   * Executes lint in the specified [mode].
   *
   * @param mode the [LintMode] to run
   */
  fun execute(mode: LintMode) {
    when (mode) {
      LintMode.CHECK_SCRIPT_CONSISTENCY -> runCheckScriptConsistency()
      LintMode.LIST_CHECKS -> runAnalysis(mode, changedFiles = null)
      LintMode.FAST -> {
        val changedFiles = retrieveChangedSourceFiles()
        runAnalysis(mode, changedFiles)
      }
      LintMode.FULL -> runAnalysis(mode, changedFiles = null)
    }
  }

  /**
   * Retrieves the set of changed `.kt` and `.java` files compared to the develop branch.
   *
   * Uses [GitClient] to diff against develop, then filters locally by file extension.
   */
  fun retrieveChangedSourceFiles(): Set<String> {
    val gitClient = GitClient(repoRoot, "develop", commandExecutor)
    return gitClient.changedFiles.filter {
      it.endsWith(".kt") || it.endsWith(".java")
    }.toSet()
  }

  /**
   * Runs lint analysis in the given [mode] with optional [changedFiles] for incremental runs.
   */
  private fun runAnalysis(mode: LintMode, changedFiles: Set<String>?) {
    val workingDirectory = Files.createTempDirectory("lint_analysis").toFile()
    val timer = if (showTimer) {
      ElapsedTimeDisplayer(CoroutineScope(scriptBgDispatcher))
    } else {
      null
    }

    timer?.start()

    try {
      timer?.clearLine()
      println("Using ${workingDirectory.absolutePath} as an intermediary working directory")

      val modeDescription = when (mode) {
        LintMode.FAST ->
          "Running linter in 'fast' mode with ${changedFiles?.size ?: 0} " +
            "changed source file(s)."
        LintMode.FULL ->
          "Running linter in 'full' mode with all repository files."
        LintMode.LIST_CHECKS ->
          "Running linter in 'list-checks' mode."
        else -> ""
      }
      println(modeDescription)

      val isListChecksMode = mode == LintMode.LIST_CHECKS
      val additionalDisabledChecks = when (mode) {
        LintMode.FAST -> LintCheckCatalog.computeChecksToDisableInIncrementalRun()
        LintMode.FULL -> LintCheckCatalog.computeChecksToDisableInFullRun()
        else -> emptySet()
      }
      val reportUnusedEnum = explicitChecks.isEmpty() && mode == LintMode.FULL

      val lintAnalyzer = AndroidLintAnalyzer(
        repoRoot = repoRoot,
        workingDirectory = workingDirectory,
        commandExecutor = commandExecutor,
        exemptionProtoPath = exemptionProtoPath,
        groupByIssueSeverity = groupByIssueSeverity,
        timer = timer,
        reportUnusedEnum = reportUnusedEnum,
        listChecks = isListChecksMode,
        checks = explicitChecks,
        changedFiles = changedFiles,
        additionalDisabledChecks = additionalDisabledChecks
      )

      lintAnalyzer.runAnalysis()
    } finally {
      val totalTimeMs = timer?.stop()
      if (totalTimeMs != null) {
        println("Total execution time: ${totalTimeMs.toFormattedDuration()}")
      }
    }
  }

  /**
   * Runs the check-script-consistency mode.
   *
   * Compares the catalog's manually-curated [LintCheckCatalog.allKnownChecks] against the
   * authoritative [LintCheckCatalog.registryChecks] (sourced from [BuiltinIssueRegistry]).
   * Fails if any checks are missing from the catalog (newly added by a lint upgrade) or
   * if the catalog contains checks that the registry doesn't recognize (removed/renamed).
   */
  fun runCheckScriptConsistency() {
    println("Running check-script-consistency mode...")

    val registryChecks = LintCheckCatalog.registryChecks
    val catalogChecks = LintCheckCatalog.allKnownChecks
    validateCatalogConsistency(registryChecks, catalogChecks)
  }

  /**
   * Validates that the catalog is consistent with the linter's actual checks.
   *
   * @param linterChecks the checks reported by the linter
   * @param catalogChecks the checks in [LintCheckCatalog]
   * @throws IllegalStateException if the catalog is out of sync
   */
  fun validateCatalogConsistency(
    linterChecks: Set<String>,
    catalogChecks: Set<String>
  ) {
    val missingFromCatalog = linterChecks.filter { it !in catalogChecks }.toSet()
    val extraInCatalog = catalogChecks.filter { it !in linterChecks }.toSet()

    var failed = false
    if (missingFromCatalog.isNotEmpty()) {
      println(
        "ERROR: ${missingFromCatalog.size} check(s) exist in lint " +
          "but are NOT in LintCheckCatalog:"
      )
      for (check in missingFromCatalog.sorted()) { println("  + $check") }
      println("Add these to the appropriate bucket in LintCheckCatalog.kt.")
      failed = true
    }
    if (extraInCatalog.isNotEmpty()) {
      println(
        "ERROR: ${extraInCatalog.size} check(s) are in LintCheckCatalog " +
          "but NOT recognized by lint:"
      )
      for (check in extraInCatalog.sorted()) { println("  - $check") }
      println("Remove these from LintCheckCatalog.kt.")
      failed = true
    }

    if (failed) {
      throw IllegalStateException(
        "LintCheckCatalog is out of sync with the linter. See above for details."
      )
    }

    println(
      "CHECK PASSED: LintCheckCatalog (${catalogChecks.size} checks) " +
        "is consistent with lint (${linterChecks.size} checks)."
    )
  }
}

/**
 * Manages the Android Lint analysis process.
 *
 * @param repoRoot the root directory of the repository
 * @param workingDirectory the temporary working directory for lint analysis
 * @param commandExecutor executes the specified command in the specified working directory
 * @param groupByIssueSeverity whether to group issues by severity in the output
 * @param timer optional elapsed time displayer for showing progress
 */
class AndroidLintAnalyzer(
  private val repoRoot: File,
  private val workingDirectory: File,
  private val commandExecutor: CommandExecutor,
  private val exemptionProtoPath: String = DEFAULT_PROTO_BINARY_PATH,
  private val groupByIssueSeverity: Boolean = false,
  private val timer: ElapsedTimeDisplayer? = null,
  private val reportUnusedEnum: Boolean = true,
  private val listChecks: Boolean = false,
  private val checks: List<String> = emptyList(),
  private val changedFiles: Set<String>? = null,
  private val additionalDisabledChecks: Set<String> = emptySet()
) {
  private val bazelClient = BazelClient(repoRoot, commandExecutor)
  companion object {
    private const val LINT_REPORT_FILE = "lint-report.xml"

    private val suppressLintIssues = setOf(
      // Managed via TranslateWiki, safe to suppress in lint reports.
      "MissingTranslation",
      // Gradle-specific; not relevant since project has migrated to Bazel.
      "GradleOverrides",
      // Fixing requires tedious lambda refactoring; suppression preferred.
      "SyntheticAccessor",
      // Allowed since context-specific translations may differ; false positive in lint.
      "DuplicateStrings",
      // TextViews are kept non-selectable to avoid conflicts with user interactions.
      "SelectableText",
      // TODO(#5887): Re-enable below checks once the AAR/JAR files issue is fixed.
      "UnusedResources",
      "UnusedAttribute",
      "UnknownNullness",
      "MergeRootFrame",
      "OldTargetApi"
    )
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
      timer = timer,
      reportUnusedEnum = reportUnusedEnum,
      listChecks = listChecks
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
      kotlinCompilerVersion = extractKotlinMajorVersion(kotlinVersion),
      suppressLintIssues = suppressLintIssues + additionalDisabledChecks,
      listChecks = listChecks,
      checks = checks
    )

    lintRunner.runLint(cliArgs)
  }

  /** Generates the project description XML file. */
  private fun generateProjectDescription(): File {
    val lintProjectDescription = LintProjectDescription(
      repoRoot = repoRoot,
      workingDirectory = workingDirectory,
      commandExecutor = commandExecutor,
      changedFiles = changedFiles
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
 * Wrapper class to run lint with timeout protection.
 *
 * The lint tool sometimes hangs on certain systems after writing the report
 * (likely due to lingering non-daemon threads in the native Java process).
 * Since lint runs natively inside this script as a Java application,
 * it may continue running indefinitely unless explicitly stopped.
 *
 * This utility ensures that lint execution terminates within a bounded time,
 * preventing hangs by enforcing a timeout and interrupting the process if
 * it fails to complete.
 */
class LintTimeoutWrapper(
  private val cliArgs: Array<String>,
  private val timeoutMinutes: Long
) {
  @Volatile
  private var exitCode: Int = -1

  @Volatile
  private var completed = false

  @Volatile
  private var timedOut = false

  /**
   * Runs lint analysis with a timeout.
   *
   * @return the exit code from the lint process
   * @throws IllegalStateException if lint doesn't finish within the specified duration
   */
  fun runWithTimeout(): Int {
    val lintThread = thread(start = true, name = "lint-runner") {
      try {
        exitCode = LintCli().run(cliArgs)
        completed = true
      } catch (e: Exception) {
        e.printStackTrace()
        exitCode = 1
        completed = true
      }
    }

    val timeoutMillis = TimeUnit.MINUTES.toMillis(timeoutMinutes)
    val startTime = System.currentTimeMillis()

    while (!completed && (System.currentTimeMillis() - startTime) < timeoutMillis) {
      Thread.sleep(100)
    }

    if (!completed) {
      timedOut = true
      // Attempt to interrupt the lint thread
      lintThread.interrupt()

      // Short time to respond to interruption
      Thread.sleep(1000)

      throw IllegalStateException(
        "Lint analysis timed out after $timeoutMinutes minutes. " +
          "This can happen if the Lint tool hanged or is taking excess execution time. " +
          "Consider increasing the timeout via --processTimeout=<minutes> if needed."
      )
    }

    return exitCode
  }
}

/**
 * Runs the Android Lint tool and reports issues.
 *
 * @param reportFile the XML file where lint results will be written
 * @param projectDescriptionFile the XML file containing project configuration
 * @param groupByIssueSeverity whether to group issues by severity in the output
 * @param timer optional elapsed time displayer for clearing display lines
 * @param reportUnusedEnum whether to report unused exemptions in the output
 */
class AndroidLintRunner(
  private val reportFile: File,
  private val projectDescriptionFile: File,
  private val repoRoot: File,
  private val exemptionProtoPath: String = DEFAULT_PROTO_BINARY_PATH,
  private val groupByIssueSeverity: Boolean = false,
  private val timer: ElapsedTimeDisplayer? = null,
  private val reportUnusedEnum: Boolean = true,
  private val listChecks: Boolean = false
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
    println("Starting lint analysis with $DEFAULT_PROCESS_TIMEOUT_MINUTES minute timeout.")

    val exitCode = try {
      val wrapper = LintTimeoutWrapper(cliArgs, DEFAULT_PROCESS_TIMEOUT_MINUTES)
      wrapper.runWithTimeout()
    } catch (e: IllegalStateException) {
      exitProcess(1)
    }

    // Allow exit code 1(ISSUES_FOUND) since it indicates issues with
    // severity Error which is being handled by LintAnalysisReporter.
    if (exitCode != SUCCESS && exitCode != ISSUES_FOUND) {
      val reason = ERROR_CODE_MESSAGES[exitCode] ?: "Unknown failure or internal error"
      error("Lint analysis failed with exit code $exitCode: $reason")
    }

    if (!listChecks) {
      reportLintIssues()
    }
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
    kotlinCompilerVersion: String,
    suppressLintIssues: Set<String>,
    listChecks: Boolean = false,
    checks: List<String> = emptyList()
  ): Array<String> {
    prepareJdkEnvironment(jdkHome)
    if (listChecks) {
      return arrayOf(
        "--list",
        "--sdk-home", getAndroidSdkPath(),
        "--client-id", LINT_CLIENT_ID
      )
    }

    val arguments = mutableListOf(
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
    if (suppressLintIssues.isNotEmpty()) {
      arguments.add("--disable")
      arguments.add(suppressLintIssues.joinToString(","))
    }
    if (checks.isNotEmpty()) {
      arguments.add("--check")
      arguments.add(checks.joinToString(","))
    }
    return arguments.toTypedArray()
  }

  private fun reportLintIssues() {
    timer?.clearLine()

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

    val redundantExemptions = if (reportUnusedEnum) {
      reporter.findRedundantExemptions(
        issues = allIssues,
        exemptions = exemptions.androidLintExemptionList
      )
    } else {
      emptyMap()
    }

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
