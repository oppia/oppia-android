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
import kotlin.math.log10

private const val USAGE =
  "Usage: bazel run //scripts:pre_push_checks -- </path/to/repo_root> [autofix]"

fun main(vararg args: String) {
  require(args.size in 1..2) { USAGE }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) {
      "Expected provided repository root to be an existing directory: ${args[0]}."
    }
  }
  check(repoRoot.exists()) { USAGE }
  if (args.size == 2) check(args[1] == "autofix") { USAGE }
  val autofix = args.size == 2

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
      prePushChecker.runPrePushChecks(autofix)
    }
  }
}

class PrePushChecks(
  private val repoRoot: File,
  private val prePushLog: File,
  private val bazelClient: BazelClient,
  private val commandExecutor: CommandExecutor,
  private val logger: Logger,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  fun runPrePushChecks(autofix: Boolean) {
    val preBuildTargetsDeferred = CoroutineScope(scriptBgDispatcher).async {
      val targetsToBuild = SUITES_TO_RUN.map(CheckSuite::deployTarget)
      bazelClient.build(*targetsToBuild.toTypedArray(), allowFailures = true)
    }
    val (buildExitCode, buildOutputLines) = logger.printAndAwaitResult(
      prefix = "Pre-building ${SUITES_TO_RUN.size} static check suites",
      delayMs = SuiteSpeed.REASONABLE.runningCheckFrequencyMs,
      preBuildTargetsDeferred
    )
    if (buildExitCode != 0) {
      logger.println("failed!", color = Logger.ConsoleColor.RED)
      logger.println()
      buildOutputLines.forEach(logger::println)
      logger.println()
      throw Exception("One or more static checks failed to build. See build logs above.")
    }

    logger.println("passed!", color = Logger.ConsoleColor.GREEN)

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
        val args = if (autofix && checkSuite.hasAutofixCommand) {
          checkSuite.createAutofixJavaRunArgs(repoRoot)
        } else checkSuite.createCheckJavaRunArgs(repoRoot)
        val result = commandExecutor.executeCommand(repoRoot, "java", *args)
        return@async result.exitCode to result.output
      }
    }
    val longestSuiteNameLength = SUITES_TO_RUN.maxOf { it.name.length }
    val failures = suiteResults.mapIndexedNotNull { index, (checkSuite, runSuiteDeferred) ->
      val indexPrefix = (index + 1).padToString(SUITES_TO_RUN.size.countDigits())
      val namePostfix = " ".repeat(longestSuiteNameLength - checkSuite.name.length)
      val action = if (autofix && checkSuite.hasAutofixCommand) "Fixing" else "Checking"
      val (exitCode, outputLines) =
        logger.printAndAwaitResult(
          prefix = "[$indexPrefix/${SUITES_TO_RUN.size} - ${checkSuite.name}$namePostfix] $action",
          delayMs = checkSuite.speed.runningCheckFrequencyMs,
          runSuiteDeferred
        )
      return@mapIndexedNotNull if (exitCode != 0) {
        logger.println("failed!", color = Logger.ConsoleColor.RED)
        checkSuite to outputLines
      } else null.also { logger.println("passed!", color = Logger.ConsoleColor.GREEN) }
    }.toMap()
    failures.forEach { (suite, failureLines) ->
      logger.println()
      logger.printSection(suite.name)
      logger.println()
      failureLines.forEach(logger::println)
      logger.println()
      logger.println("Re-run command:")
      logger.println("  ${suite.createCheckBazelRunCommand()}", color = Logger.ConsoleColor.MAGENTA)
    }
    logger.println("\n${"*".repeat(n = CONSOLE_COL_LIMIT)}\n")

    val timeSpentMs = System.currentTimeMillis() - startTimeMs
    logger.println("Checks finished in ${timeSpentMs / 1000}.${timeSpentMs % 1000}s.")
    logger.println()

    if (failures.isNotEmpty()) {
      logger.println(
        "${failures.size}/${SUITES_TO_RUN.size} suites failed.", color = Logger.ConsoleColor.RED
      )

      if (!autofix && failures.keys.any { it.hasAutofixCommand }) {
        logger.println()
        logger.println("You can try to autofix some of the failures above using:")
        logger.println(
          "  bazel run //scripts:pre_push_checks -- $(pwd) autofix",
          color = Logger.ConsoleColor.MAGENTA
        )
      }

      // The IntelliJ-clickable version is a bit hacky. See:
      // https://stackoverflow.com/a/30941328/3689782.
      println()
      println("Log results can be found at:")
      println("  Relative: ./${prePushLog.toRelativeString(repoRoot)}")
      println("  Clickable: file://${prePushLog.path}")
      println("  IntelliJ.log(${prePushLog.toRelativeString(repoRoot)}:1)")
      println()

      error("Checks failed.")
    } else {
      logger.println("All ${SUITES_TO_RUN.size} suites pass.", color = Logger.ConsoleColor.GREEN)
    }
  }

  companion object {
    private const val CONSOLE_COL_LIMIT = 80

    class Logger(
      private val plainStreams: List<PrintStream>,
      private val colorStreams: List<PrintStream>
    ) {
      private val allStreams by lazy { plainStreams + colorStreams }

      fun print(str: String) = allStreams.forEach { it.print(str) }
      fun println() = allStreams.forEach { it.println() }
      fun println(str: String) = allStreams.forEach { it.println(str) }
      fun println(str: String, color: ConsoleColor) {
        startColor(color)
        print(str)
        endColor()
        println()
      }

      // Wrap the string with a color to render per:
      // https://www.tutorialspoint.com/how-to-output-colored-text-to-a-linux-terminal.
      private fun startColor(color: ConsoleColor) {
        colorStreams.forEach { it.print("\u001B[1;${color.colorCode}m") }
      }

      private fun endColor() = colorStreams.forEach { it.print("\u001B[0m") }

      enum class ConsoleColor(val colorCode: Int) {
        RED(colorCode = 31),
        GREEN(colorCode = 32),
        MAGENTA(colorCode = 35),
        CYAN(colorCode = 36)
      }
    }

    private data class CheckSuite(
      val name: String,
      val bazelTarget: String,
      val speed: SuiteSpeed,
      val extraCheckArgs: List<String>,
      val autofixArgs: List<String>?
    ) {
      val deployTarget = "${bazelTarget}_deploy.jar"
      val hasAutofixCommand get() = autofixArgs != null
      private val targetName get() = bazelTarget.substringAfter(':')
      private val deployJarPath get() = "bazel-bin/scripts/${targetName}_deploy.jar"

      fun createCheckBazelRunCommand(): String =
        "bazel run $bazelTarget -- $(pwd) ${extraCheckArgs.joinToString(separator = " ")}".trim()

      fun createCheckJavaRunArgs(repoRoot: File): Array<String> =
        arrayOf("-jar", File(repoRoot, deployJarPath).path, repoRoot.path) + extraCheckArgs

      fun createAutofixJavaRunArgs(repoRoot: File): Array<String> {
        checkNotNull(autofixArgs) { "Expected suite to support auto-fixing." }
        return arrayOf("-jar", File(repoRoot, deployJarPath).path, repoRoot.path) + autofixArgs
      }
    }

    private enum class SuiteSpeed(val runningCheckFrequencyMs: Long) {
      FAST(runningCheckFrequencyMs = 35L),
      REASONABLE(runningCheckFrequencyMs = 125L),
      SLOW(runningCheckFrequencyMs = 400L),
    }

    // These are run in listed order.
    private val SUITES_TO_RUN = listOf(
      createSuite(
        name = "XML style", target = "//scripts:xml_syntax_check", speed = SuiteSpeed.REASONABLE
      ),
      createSuite(
        name = "Proto style",
        target = "//scripts:buf",
        speed = SuiteSpeed.REASONABLE,
        extraCheckArgs = listOf("check"),
        extraAutofixArgs = listOf("fix")
      ),
      createSuite(
        name = "Bazel style",
        target = "//scripts:buildifier",
        speed = SuiteSpeed.REASONABLE,
        extraCheckArgs = listOf("check"),
        extraAutofixArgs = listOf("fix")
      ),
      createSuite(
        name = "Java style", target = "//scripts:checkstyle", speed = SuiteSpeed.REASONABLE
      ),
      createSuite(
        name = "Kotlin style",
        target = "//scripts:ktlint",
        speed = SuiteSpeed.SLOW,
        extraCheckArgs = listOf("check"),
        extraAutofixArgs = listOf("fix")
      ),
      createSuite(
        name = "Test files",
        target = "//scripts:test_file_check",
        speed = SuiteSpeed.FAST
      ),
      createSuite(
        name = "TextView styles",
        target = "//scripts:check_textview_styles",
        speed = SuiteSpeed.FAST
      ),
      createSuite(
        name = "Translations",
        target = "//scripts:string_resource_validation_check",
        speed = SuiteSpeed.FAST
      ),
      createSuite(
        name = "A11y labels",
        target = "//scripts:accessibility_label_check",
        speed = SuiteSpeed.REASONABLE,
        extraCheckArgs = listOf("app/src/main/AndroidManifest.xml")
      ),
      createSuite(
        name = "KDocs",
        target = "//scripts:kdoc_validity_check",
        speed = SuiteSpeed.REASONABLE
      ),
      createSuite(
        name = "Regex checks",
        target = "//scripts:regex_pattern_validation_check",
        speed = SuiteSpeed.SLOW
      ),
      createSuite(
        name = "TODOs",
        target = "//scripts:todo_open_check",
        speed = SuiteSpeed.SLOW,
        extraAutofixArgs = listOf("regenerate")
      ),
      createSuite(
        name = "License texts",
        target = "//scripts:license_texts_check",
        speed = SuiteSpeed.FAST,
        extraCheckArgs = listOf("app/src/main/res/values/third_party_dependencies.xml")
      ),
      createSuite(
        name = "Maven licenses",
        target = "//scripts:maven_dependencies_list_check",
        speed = SuiteSpeed.SLOW,
        extraCheckArgs = listOf("third_party/maven_install.json")
      ),
    )

    private fun createSuite(
      name: String,
      target: String,
      speed: SuiteSpeed,
      extraCheckArgs: List<String> = emptyList(),
      extraAutofixArgs: List<String>? = null
    ): CheckSuite = CheckSuite(name, target, speed, extraCheckArgs, extraAutofixArgs)

    private fun Logger.printSection(label: String) {
      val remainingChars = CONSOLE_COL_LIMIT - (label.length + 2)
      if (remainingChars >= 2) {
        val prefixLength = remainingChars / 2
        val postfixLength = CONSOLE_COL_LIMIT - label.length - prefixLength - 2
        val prefix = "*".repeat(prefixLength)
        val postfix = "*".repeat(postfixLength)
        println("$prefix $label $postfix", color = Logger.ConsoleColor.CYAN)
      } else println(label, color = Logger.ConsoleColor.CYAN)
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

private fun Int.padToString(digitCount: Int): String = " ".repeat(digitCount - countDigits()) + this

private fun Int.countDigits(): Int = log10(toFloat()).toInt() + 1
