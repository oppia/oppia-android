package org.oppia.android.scripts.ci

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/**
 * The main entrypoint for all checks that should be run before pushing a Git branch.
 *
 * This script automatically runs a suite of pre-selected checks that, together, require about 1-2
 * minutes to run. These checks catch some of the most common issues that a reviewer is likely to
 * bring up during review, and thus they should always be fixed before pushing any code to remote
 * origins (like GitHub).
 *
 * The script outputs several noteworthy pieces of information:
 * - The estimate ongoing progress of each check (these estimates may be off slightly, but serve as
 *   a rough approximation to track check progress).
 * - The pass/fail results of each check.
 * - For failing checks, the complete output of the failure along with a command to re-run that
 *   specific check in isolation.
 * - A list of all re-run commands at the end of the results.
 * - A local file copy of the failure logs (in case they need to be uploaded for help).
 *
 * Usage:
 *   bazel run //scripts:pre_push_checks -- <path_to_repo_root>
 *
 * Arguments:
 * - path_to_repo_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:pre_push_checks -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.size == 1) { "Usage: bazel run //scripts:pre_push_checks -- </path/to/repo_root>" }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) {
      "Expected provided repository root to be an existing directory: ${args[0]}."
    }
  }
  val prePushLog = File(repoRoot, "scripts/pre-push-failures.log")
  PrintStream(prePushLog.outputStream()).use { prePushStream ->
    val logger = PrePushChecks.Companion.Logger(
      plainStreams = listOf(prePushStream), colorStreams = listOf(System.out)
    )
    ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
      // Use a longer timeout since some of the checks can take a while to run (especially if
      // targets need to be built first).
      val executor =
        CommandExecutorImpl(
          scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
        )
      val bazelClient = BazelClient(repoRoot, executor)
      val prePushChecker =
        PrePushChecks(repoRoot, prePushLog, bazelClient, executor, logger, scriptBgDispatcher)
      prePushChecker.runPrePushChecks()
    }
  }
}

/**
 * Utility for running a series of pre-push checks.
 *
 * @property repoRoot the absolute [File] corresponding to the root of the inspected repository
 * @property prePushLog the file that should have the full log of pre-push results be written to it
 * @property bazelClient a [BazelClient] configured for a single repository at [repoRoot]
 * @property commandExecutor the [CommandExecutor] to be used for command execution. Note that this
 *     executor should have an especially long timeout period.
 * @property logger the [Logger] that will be used for outputting check results
 * @property scriptBgDispatcher a [ScriptBackgroundCoroutineDispatcher] to be used for background
 *     task execution
 */
class PrePushChecks(
  private val repoRoot: File,
  private val prePushLog: File,
  private val bazelClient: BazelClient,
  private val commandExecutor: CommandExecutor,
  private val logger: Logger,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  /** Runs a series of checks that should pass before pushing a Git branch to a remote origin. */
  fun runPrePushChecks() {
    val preBuildTargetsDeferred = CoroutineScope(scriptBgDispatcher).async {
      val targetsToBuild = SUITES_TO_RUN.map(CheckSuite::deployTarget)
      bazelClient.build(*targetsToBuild.toTypedArray())
    }
    logger.printAndAwaitResult(
      prefix = "Pre-building ${SUITES_TO_RUN.size} check suites",
      delayMs = SuiteSpeed.REASONABLE.runningCheckFrequencyMs,
      preBuildTargetsDeferred
    )
    // Failures will result in an exception being thrown.
    logger.println("passed!", color = Logger.ConsoleTextColor.GREEN)

    val startTimeMs = System.currentTimeMillis()
    val suiteRunOrders = SUITES_TO_RUN.withIndex().sortedByDescending { (_, checkSuite) ->
      checkSuite.speed
    }.mapIndexed { sortedIndex, (originalIndex, _) -> originalIndex to sortedIndex }.toMap()
    val suiteResults = SUITES_TO_RUN.mapIndexed { index, checkSuite ->
      // Run deployment Jars of the scripts directly, and start them in the reverse order of their
      // expected speed (i.e. start the most expensive first). Use some artificial delays to give
      // the more expensive scripts time to start so that they can run for a while when the user is
      // waiting for the faster scripts to finish. Note that some scripts may contend if they use
      // Bazel internally.
      checkSuite to CoroutineScope(scriptBgDispatcher).async {
        delay(suiteRunOrders.getValue(index) * 10L)
        val result =
          commandExecutor.executeCommand(repoRoot, "java", *checkSuite.createJavaRunArgs(repoRoot))
        return@async result.exitCode to result.output
      }
    }
    val failures = suiteResults.mapIndexedNotNull { index, (checkSuite, runSuiteDeferred) ->
      val (exitCode, outputLines) =
        logger.printAndAwaitResult(
          prefix = "[${index + 1}/${SUITES_TO_RUN.size} - ${checkSuite.name}] Checking",
          delayMs = checkSuite.speed.runningCheckFrequencyMs,
          runSuiteDeferred
        )
      return@mapIndexedNotNull if (exitCode != 0) {
        logger.println("failed!", color = Logger.ConsoleTextColor.RED)
        checkSuite to outputLines
      } else null.also { logger.println("passed!", color = Logger.ConsoleTextColor.GREEN) }
    }.toMap()
    failures.forEach { (suite, failureLines) ->
      logger.println()
      logger.printSection(suite.name)
      logger.println()
      failureLines.forEach(logger::println)
      logger.println()
      logger.println("Re-run command:")
      logger.println("  ${suite.createBazelRunCommand()}", color = Logger.ConsoleTextColor.MAGENTA)
    }
    logger.println("\n${"*".repeat(n = CONSOLE_COL_LIMIT)}\n")

    val timeSpentMs = System.currentTimeMillis() - startTimeMs
    logger.println("Checks finished in ${timeSpentMs / 1000}.${timeSpentMs % 1000}s.")
    logger.println()

    if (failures.isNotEmpty()) {
      logger.println(
        "${failures.size}/${SUITES_TO_RUN.size} suites failed.", color = Logger.ConsoleTextColor.RED
      )

      logger.println()
      logger.println("All commands to re-run:", color = Logger.ConsoleTextColor.MAGENTA)
      failures.keys.forEach { logger.println("  ${it.createBazelRunCommand()}") }
      logger.println()

      // The IntelliJ-clickable version is a bit hacky. See:
      // https://stackoverflow.com/a/30941328/3689782.
      println("Log results can be found at:")
      println("  Relative: ./${prePushLog.toRelativeString(repoRoot)}")
      println("  Clickable: file://${prePushLog.path}")
      println("  IntelliJ.log(${prePushLog.toRelativeString(repoRoot)}:1)")
      println()

      error("Checks failed.")
    } else {
      logger.println(
        "All ${SUITES_TO_RUN.size} suites pass.", color = Logger.ConsoleTextColor.GREEN
      )
    }
  }

  companion object {
    private const val CONSOLE_COL_LIMIT = 80

    /**
     * A de-multiplexing console logger that supports colors.
     *
     * @property plainStreams the list of [PrintStream]s to output lines to without colors
     * @property colorStreams the list of [PrintStream]s to output lines to with escaped colors
     */
    class Logger(
      private val plainStreams: List<PrintStream>,
      private val colorStreams: List<PrintStream>
    ) {
      private val allStreams by lazy { plainStreams + colorStreams }

      /** Prints a string [str] to the output streams without color or a newline. */
      fun print(str: String) = allStreams.forEach { it.print(str) }

      /** Prints a newline to the output streams. */
      fun println() = allStreams.forEach { it.println() }

      /** Prints a string [str] with a newline to the output streams without color. */
      fun println(str: String) = allStreams.forEach { it.println(str) }

      /** Prints a string [str] with the specified [color] to the output streams, with a newline. */
      fun println(str: String, color: ConsoleTextColor) {
        startColor(color)
        print(str)
        endColor()
        println()
      }

      // Wrap the string with a color to render per:
      // https://www.tutorialspoint.com/how-to-output-colored-text-to-a-linux-terminal.
      private fun startColor(color: ConsoleTextColor) {
        colorStreams.forEach { it.print("\u001B[1;${color.colorCode}m") }
      }

      private fun endColor() = colorStreams.forEach { it.print("\u001B[0m") }

      /**
       * An escapable representation of a console color that can be used when printing text with
       * [Logger].
       *
       * Note that the actual RGB values of the rendered colors is TTY-dependent.
       *
       * @property colorCode the escape code used when representing the color
       */
      enum class ConsoleTextColor(val colorCode: Int) {
        /** Represents a red color. */
        RED(colorCode = 31),

        /** Represents a green color. */
        GREEN(colorCode = 32),

        /** Represents a magenta color. */
        MAGENTA(colorCode = 35),

        /** Represents a cyan color. */
        CYAN(colorCode = 36)
      }
    }

    private data class CheckSuite(
      val name: String,
      val bazelTarget: String,
      val speed: SuiteSpeed,
      val extraArgs: List<String>
    ) {
      val deployTarget = "${bazelTarget}_deploy.jar"
      private val targetName get() = bazelTarget.substringAfter(':')
      private val deployJarPath get() = "bazel-bin/scripts/${targetName}_deploy.jar"

      fun createBazelRunCommand(): String =
        "bazel run $bazelTarget -- $(pwd) ${extraArgs.joinToString(separator = " ")}".trim()

      fun createJavaRunArgs(repoRoot: File): Array<String> =
        arrayOf("-jar", File(repoRoot, deployJarPath).path, repoRoot.path) + extraArgs
    }

    private enum class SuiteSpeed(val runningCheckFrequencyMs: Long) {
      FAST(runningCheckFrequencyMs = 35L),
      REASONABLE(runningCheckFrequencyMs = 125L),
      SLOW(runningCheckFrequencyMs = 400L),
      VERY_SLOW(runningCheckFrequencyMs = 700L)
    }

    // These are run in listed order.
    private val SUITES_TO_RUN = listOf(
      createSuite(
        name = "XML style", target = "//scripts:xml_syntax_check", speed = SuiteSpeed.FAST
      ),
      createSuite(name = "Proto style", target = "//scripts:buf", speed = SuiteSpeed.FAST),
      createSuite(
        name = "Build/Bazel style",
        target = "//scripts:buildifier",
        speed = SuiteSpeed.REASONABLE,
        "check"
      ),
      createSuite(
        name = "Java style", target = "//scripts:checkstyle", speed = SuiteSpeed.REASONABLE
      ),
      createSuite(
        name = "Kotlin style",
        target = "//scripts:ktlint",
        speed = SuiteSpeed.SLOW,
        "check"
      ),
      createSuite(
        name = "Resource validation",
        target = "//scripts:string_resource_validation_check",
        speed = SuiteSpeed.FAST
      ),
      createSuite(
        name = "Test file presence",
        target = "//scripts:test_file_check",
        speed = SuiteSpeed.FAST
      ),
      createSuite(
        name = "Activity a11y labels presence",
        target = "//scripts:accessibility_label_check",
        speed = SuiteSpeed.REASONABLE,
        "app/src/main/AndroidManifest.xml"
      ),
      createSuite(
        name = "KDoc validation",
        target = "//scripts:kdoc_validity_check",
        speed = SuiteSpeed.REASONABLE
      ),
      createSuite(
        name = "Regex validation",
        target = "//scripts:regex_pattern_validation_check",
        speed = SuiteSpeed.SLOW
      ),
      createSuite(
        name = "Maven deps validation (app)",
        target = "//scripts:validate_maven_dependencies",
        speed = SuiteSpeed.VERY_SLOW,
        "third_party/versions/direct_maven_versions.bzl",
        "third_party/versions/transitive_maven_versions.bzl",
        "third_party/versions/maven_install.json",
        "//third_party",
        "//..."
      ),
      createSuite(
        name = "Maven deps validation (scripts)",
        target = "//scripts:validate_maven_dependencies",
        speed = SuiteSpeed.REASONABLE,
        "scripts/third_party/versions/direct_maven_versions.bzl",
        "scripts/third_party/versions/transitive_maven_versions.bzl",
        "scripts/third_party/versions/maven_install.json",
        "//scripts/third_party",
        "//scripts/..."
      ),
      createSuite(
        name = "Maven license validation (app)",
        target = "//scripts:maven_dependencies_list_check",
        speed = SuiteSpeed.SLOW,
        "third_party/versions/maven_install.json",
      ),
    )

    private fun createSuite(
      name: String,
      target: String,
      speed: SuiteSpeed,
      vararg extraArgs: String
    ): CheckSuite = CheckSuite(name, target, speed, extraArgs.toList())

    private fun Logger.printSection(label: String) {
      val remainingChars = CONSOLE_COL_LIMIT - (label.length + 2)
      if (remainingChars >= 2) {
        val prefixLength = remainingChars / 2
        val postfixLength = CONSOLE_COL_LIMIT - label.length - prefixLength - 2
        val prefix = "*".repeat(prefixLength)
        val postfix = "*".repeat(postfixLength)
        println("$prefix $label $postfix", color = Logger.ConsoleTextColor.CYAN)
      } else println(label, color = Logger.ConsoleTextColor.CYAN)
    }

    private fun <T> Logger.printAndAwaitResult(
      prefix: String,
      delayMs: Long,
      deferred: Deferred<T>
    ): T {
      val numberOfChecks = CONSOLE_COL_LIMIT - prefix.length - 7 // 7 chars for the result.
      var completionCheckCount = 0
      return runBlocking {
        print(prefix)
        for (i in 0 until numberOfChecks) {
          delay(delayMs)
          if (deferred.isCompleted) break
          completionCheckCount++
          print(".")
        }
        // Print the remaining dots, if any, in quick succession.
        repeat(numberOfChecks - completionCheckCount) {
          print(".")
          delay(5)
        }
        deferred.await()
      }
    }
  }
}
