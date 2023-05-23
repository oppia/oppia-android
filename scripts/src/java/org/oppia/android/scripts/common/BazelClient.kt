package org.oppia.android.scripts.common

import java.io.File
import java.lang.IllegalArgumentException
import java.util.Locale

/**
 * Utility class to query & interact with a Bazel workspace on the local filesystem (residing within
 * the specified root directory). The provided [universeScope] is the default Sky Query scope to use
 * in corresponding query operations.
 */
class BazelClient(
  private val rootDirectory: File,
  private val commandExecutor: CommandExecutor,
  private val universeScope: String = "//..."
) {
  /**
   * Attempts to build the provided patterns using ``bazel build``.
   *
   * @param patterns one or more build patterns, as ``bazel build`` usually accepts
   * @param keepGoing whether to continue building if any targets in the provided patterns fails to
   *     build. This defaults to false.
   * @param allowFailures whether to throw an exception on a build failure, or to instead capture
   *     the results of the failure as part of the returned [Result.outputLines]. This defaults to
   *     false.
   * @param configProfiles the set of configuration profiles to enable, e.g. using
   *     ``bazel --config=<profile_name>``. This defaults to an empty set (i.e. no profiles).
   * @param reportProgress a callback that continuously receives two parameters during the build:
   *     the first being the number of targets built, and the second being the total to build. Note
   *     that the total may change throughout the build since Bazel computes its action graph
   *     alongside building for performance reasons. If ``null`` is provided then progress isn't
   *     reported. This defaults to ``null``.
   * @return the [Result] of the attempted build
   */
  fun build(
    vararg patterns: String,
    keepGoing: Boolean = false,
    allowFailures: Boolean = false,
    configProfiles: Set<String> = emptySet(),
    reportProgress: ((Int, Int) -> Unit)? = null
  ): Result {
    val args = listOfNotNull(
      if (reportProgress == null) "--noshow_progress" else null,
      "--keep_going".takeIf { keepGoing }
    ) + configProfiles.map { "--config=$it" } + listOf("--") + patterns
    return if (reportProgress != null) {
      executeBazelCommandWithMonitoring(
        "build",
        *args.toTypedArray(),
        allowAllFailures = allowFailures,
        reportProgress = reportProgress
      )
    } else executeBazelCommand("build", *args.toTypedArray(), allowAllFailures = allowFailures)
  }

  /**
   * Attempts to run the provided test patterns using ``bazel test``.
   *
   * @param patterns one or more test patterns, as ``bazel test`` usually accepts
   * @param keepGoing whether to continue building if any targets in the provided patterns fails to
   *     build. This defaults to false.
   * @param allowFailures whether to throw an exception on a build or test failure, or to instead
   *     capture the results of the failure as part of the returned [Result.outputLines]. This
   *     defaults to false.
   * @param configProfiles the set of configuration profiles to enable, e.g. using
   *     ``bazel --config=<profile_name>``. This defaults to an empty set (i.e. no profiles).
   * @param reportProgress a callback for monitoring test progress in the same way as [build]. This
   *     defaults to ``null``.
   * @return the [Result] of the attempted test
   */
  fun test(
    vararg patterns: String,
    keepGoing: Boolean = false,
    allowFailures: Boolean = false,
    configProfiles: Set<String> = emptySet(),
    reportProgress: ((Int, Int) -> Unit)? = null
  ): Result {
    val args = listOfNotNull(
      if (reportProgress == null) "--noshow_progress" else null,
      "--keep_going".takeIf { keepGoing }
    ) + configProfiles.map { "--config=$it" } + listOf("--") + patterns
    return if (reportProgress != null) {
      executeBazelCommandWithMonitoring(
        "test",
        *args.toTypedArray(),
        allowAllFailures = allowFailures,
        reportProgress = reportProgress
      )
    } else executeBazelCommand("test", *args.toTypedArray(), allowAllFailures = allowFailures)
  }

  /**
   * Attempts to run the specified Bazel binary target using ``bazel run``.
   *
   * @param target the binary target that ``bazel run`` can accept
   * @param args zero or more arguments to pass to the binary being run
   * @param allowFailures whether to throw an exception on a build or run failure, or to instead
   *     capture the results of the failure as part of the returned [Result.outputLines]. This
   *     defaults to false.
   * @param silenceBazelOutput whether to omit all Bazel-specific output lines from the returned
   *     [Result]. This is especially useful when the output of the script needs to be parsed,
   *     interpreted, or printed to the user. This defaults to true.
   * @param monitorOutputLines a callback that receives each output line of the underlying
   *     ``bazel run`` command as it's encountered. If ``null``, no monitoring will take place. This
   *     defaults to ``null``.
   * @return the [Result] of the attempted run
   */
  fun run(
    target: String,
    vararg args: String,
    allowFailures: Boolean = false,
    silenceBazelOutput: Boolean = true,
    monitorOutputLines: ((String) -> Unit)? = null
  ): Result {
    // See https://github.com/bazelbuild/bazel/issues/4867#issuecomment-830402410 for the output
    // filtering being done here.
    val silenceArgs = if (silenceBazelOutput) {
      listOf(
        "--ui_event_filters=-info,-stdout,-stderr,-progress,-warning,-start,-debug",
        "--noshow_progress"
      )
    } else emptyList()
    return executeBazelCommand(
      "run",
      *silenceArgs.toTypedArray(),
      target,
      "--",
      *args,
      allowAllFailures = allowFailures,
      includeErrorOutput = monitorOutputLines != null || allowFailures,
      combinedOutputMonitor = { line ->
        if (monitorOutputLines != null) {
          if (!silenceBazelOutput || ANOTHER_COMMAND_RUNNING_PATTERN.matchEntire(line) == null) {
            monitorOutputLines(line)
          }
        }
      }
    ).let { result ->
      result.copy(
        outputLines = result.outputLines.filter { line ->
          !silenceBazelOutput || ANOTHER_COMMAND_RUNNING_PATTERN.matchEntire(line) == null
        }
      )
    }
  }

  /** Runs ``bazel sync`` and returns the captured [Result] of the attempted sync. */
  fun sync(): Result = executeBazelCommand("sync")

  /** Runs ``bazel shutdown`` and returns the captured [Result] of the attempted shutdown. */
  fun shutdown(): Result = executeBazelCommand("shutdown")

  /**
   * Attempts to perform a query on the specific pattern using ``bazel query``.
   *
   * @param pattern a queryable Bazel target pattern, as ``bazel query`` usually accepts
   * @param withSkyQuery whether to enable Sky Query during querying, using the client's configured
   *     universe scope. This defaults to false.
   * @param allowFailures whether to throw an exception when querying targets that might be invalid
   * @return the output lines from the query
   */
  fun query(
    pattern: String,
    withSkyQuery: Boolean = false,
    allowFailures: Boolean = false
  ): List<String> {
    val args = listOfNotNull(
      "--noshow_progress",
      "--order_output=no".takeIf { withSkyQuery },
      "--universe_scope=$universeScope".takeIf { withSkyQuery },
      pattern
    )
    // Ignore queries which result in an error if allowFailures is enabled.
    val queryResults =
      executeBazelCommand(
        "query", *args.toTypedArray(), allowAllFailures = allowFailures, includeErrorOutput = false
      )
    return correctPotentiallyBrokenTargetNames(queryResults)
  }

  /** Returns all Bazel test targets in the workspace. */
  fun retrieveAllTestTargets(): List<String> = query("kind(test, //...)")

  /** Returns all Bazel file targets that correspond to each of the relative file paths provided. */
  fun retrieveBazelTargets(changedFileRelativePaths: Iterable<String>): List<String> {
    return correctPotentiallyBrokenTargetNames(
      runPotentiallyShardedQueryCommand(
        "set(%s)",
        changedFileRelativePaths,
        "--noshow_progress",
        "--keep_going",
        allowPartialFailures = true
      )
    )
  }

  /** Returns all prod targets in the workspace that depend on the list of provided targets. */
  fun retrieveDependingProdTargets(targets: Iterable<String>): List<String> {
    // Reference for the "kind exclusion": https://stackoverflow.com/a/58667282/3689782.
    return correctPotentiallyBrokenTargetNames(
      runPotentiallyShardedQueryCommand(
        "filter('^[^@]', allrdeps(set(%1\$s)) except kind(test, allrdeps(set(%1\$s))))",
        targets,
        "--noshow_progress",
        "--universe_scope=$universeScope",
        "--order_output=no"
      )
    )
  }

  /** Returns all test targets in the workspace that depend on the list of provided targets. */
  fun retrieveDependingTestTargets(targets: Iterable<String>): List<String> {
    return correctPotentiallyBrokenTargetNames(
      runPotentiallyShardedQueryCommand(
        "filter('^[^@]', kind(test, allrdeps(set(%s))))",
        targets,
        "--noshow_progress",
        "--universe_scope=$universeScope",
        "--order_output=no"
      )
    )
  }

  /**
   * Returns all test targets transitively tied to the specific Bazel BUILD/WORKSPACE files listed
   * in the provided [buildFiles] list. This may return different files than
   * [retrieveDependingTestTargets] since that method relies on the dependency graph to compute
   * affected targets whereas this assumes that any changes to BUILD files could affect any test
   * directly or indirectly tied to that BUILD file, regardless of dependencies.
   */
  fun retrieveTransitiveTestTargets(buildFiles: Iterable<String>): List<String> {
    val buildFileList = buildFiles.joinToString(",")
    // Note that this check is needed since rbuildfiles() doesn't like taking an empty list.
    return if (buildFileList.isNotEmpty()) {
      val referencingBuildFiles =
        runPotentiallyShardedQueryCommand(
          "filter('^[^@]', rbuildfiles(%s))", // Use a filter to limit the search space.
          buildFiles,
          "--noshow_progress",
          "--universe_scope=$universeScope",
          "--order_output=no",
          delimiter = ","
        ).outputLines
      // Compute only test & library siblings for each individual build file. While this is both
      // much slower than a fully combined query & can potentially miss targets, it runs
      // substantially faster per query and helps to avoid potential hanging in CI. Note also that
      // this is more correct than a combined query since it ensures that siblings checks are
      // properly unique for each file being considered (vs. searching for common siblings).
      val relevantSiblings = referencingBuildFiles.flatMap { buildFileTarget ->
        retrieveFilteredSiblings(filterRuleType = "test", buildFileTarget) +
          retrieveFilteredSiblings(filterRuleType = "android_library", buildFileTarget)
      }.toSet()
      return correctPotentiallyBrokenTargetNames(
        runPotentiallyShardedQueryCommand(
          "filter('^[^@]', kind(test, allrdeps(set(%s))))",
          relevantSiblings,
          "--noshow_progress",
          "--universe_scope=$universeScope",
          "--order_output=no"
        )
      )
    } else listOf()
  }

  /**
   * Returns the list of direct and indirect production Maven third-party dependencies on which the
   * specified binary depends.
   */
  fun retrieveThirdPartyMavenDepsListForBinary(binaryTarget: String): List<String> =
    query("deps(deps($binaryTarget) intersect //third_party/...) intersect @maven_app//...")

  private fun retrieveFilteredSiblings(
    filterRuleType: String,
    buildFileTarget: String
  ): List<String> {
    return executeBazelCommand(
      "query",
      "--noshow_progress",
      "--universe_scope=$universeScope",
      "--order_output=no",
      "kind($filterRuleType, siblings($buildFileTarget))"
    ).outputLines
  }

  private fun correctPotentiallyBrokenTargetNames(result: Result): List<String> {
    val correctedTargets = mutableListOf<String>()
    for (line in result.outputLines) {
      when {
        line.isEmpty() -> correctedTargets += line
        else -> {
          val indexes = ABSOLUTE_TARGET_PATH_PREFIX_PATTERN.findAll(line).map {
            it.range.first
          }.toList()
          if (indexes.isEmpty() || indexes.first() != 0) {
            throw IllegalArgumentException(
              "Invalid line: $line (expected to start with '//' or '@<name>//')"
            )
          }

          val targetBounds: List<Pair<Int, Int>> = indexes.mapIndexed { arrayIndex, lineIndex ->
            lineIndex to (indexes.getOrNull(arrayIndex + 1) ?: line.length)
          }
          correctedTargets += targetBounds.map { (startIndex, endIndex) ->
            line.substring(startIndex, endIndex)
          }
        }
      }
    }
    return correctedTargets
  }

  /**
   * Returns the results of a query command with a potentially large list of [values] that will be
   * split up into multiple commands to avoid overflow the system's maximum argument limit.
   *
   * Note that [queryFormatStr] is expected to have 1 string variable (which will be the
   * space-separated join of [values] or a partition of [values]).
   */
  @Suppress("SameParameterValue") // This check doesn't work correctly for varargs.
  private fun runPotentiallyShardedQueryCommand(
    queryFormatStr: String,
    values: Iterable<String>,
    vararg prefixArgs: String,
    delimiter: String = " ",
    allowPartialFailures: Boolean = false
  ): Result {
    // Split up values into partitions to ensure that the argument calls don't over-run the limit.
    var partitionCount = 0
    lateinit var partitions: List<List<String>>
    do {
      partitionCount++
      partitions = values.chunked((values.count() + 1) / partitionCount)
    } while (computeMaxArgumentLength(partitions) >= MAX_ALLOWED_ARG_STR_LENGTH)

    // Fragment the query across the partitions to ensure all values can be considered.
    val allOutputLines = partitions.flatMap { partition ->
      val lastArgument = queryFormatStr.format(Locale.US, partition.joinToString(delimiter))
      val allArguments = prefixArgs.toList() + lastArgument
      executeBazelCommand(
        "query", *allArguments.toTypedArray(), allowPartialFailures = allowPartialFailures
      ).outputLines
    }
    return Result(exitCode = 0, outputLines = allOutputLines)
  }

  private fun computeMaxArgumentLength(partitions: List<List<String>>) =
    partitions.maxOfOrNull(this::computeArgumentLength) ?: 0

  private fun computeArgumentLength(args: List<String>) = args.joinToString(" ").length

  @Suppress("SameParameterValue") // This check doesn't work correctly for varargs.
  private fun executeBazelCommandWithMonitoring(
    command: String,
    vararg arguments: String,
    allowAllFailures: Boolean = false,
    allowPartialFailures: Boolean = false,
    includeErrorOutput: Boolean = allowAllFailures,
    reportProgress: ((Int, Int) -> Unit)
  ): Result {
    return executeBazelCommand(
      command,
      "--color=yes",
      "--curses=yes",
      "--progress_in_terminal_title",
      *arguments,
      allowAllFailures = allowAllFailures,
      allowPartialFailures = allowPartialFailures,
      includeErrorOutput = includeErrorOutput,
      combinedOutputMonitor = createUpdateMonitor(reportProgress)
    )
  }

  @Suppress("SameParameterValue") // This check doesn't work correctly for varargs.
  private fun executeBazelCommand(
    command: String,
    vararg arguments: String,
    allowAllFailures: Boolean = false,
    allowPartialFailures: Boolean = false,
    includeErrorOutput: Boolean = allowAllFailures,
    combinedOutputMonitor: (String) -> Unit = {}
  ): Result {
    val result =
      commandExecutor.executeCommand(
        rootDirectory,
        command = "bazel",
        command,
        *arguments,
        includeErrorOutput = includeErrorOutput,
        standardOutputMonitor = combinedOutputMonitor,
        standardErrorMonitor = combinedOutputMonitor
      )
    // Per https://docs.bazel.build/versions/main/guide.html#what-exit-code-will-i-get error code of
    // 3 is expected for queries since it indicates that some of the arguments don't correspond to
    // valid targets. Note that this COULD result in legitimate issues being ignored, but it's
    // unlikely.
    if (!allowAllFailures) {
      val expectedExitCodes = if (allowPartialFailures) listOf(0, 3) else listOf(0)
      check(result.exitCode in expectedExitCodes) {
        "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
          "\nStandard output:\n${result.output.joinToString("\n")}" +
          "\nError output:\n${result.errorOutput.joinToString("\n")}"
      }
    }
    return Result(exitCode = result.exitCode, outputLines = result.output)
  }

  private fun createUpdateMonitor(
    reportProgress: ((Int, Int) -> Unit)
  ): (String) -> Unit {
    var lastNum = 0
    var lastDen = 0
    return { line ->
      val (newNum, newDen) = maybeUpdateProgress(line, reportProgress, lastNum, lastDen)
      lastNum = newNum
      lastDen = newDen
    }
  }

  private fun maybeUpdateProgress(
    line: String,
    reportProgress: ((Int, Int) -> Unit),
    lastNumerator: Int,
    lastDenominator: Int
  ): Pair<Int, Int> {
    val progress = line.parseProgressUpdate() ?: (lastNumerator to lastDenominator)
    val updatedNumerator = progress.first.coerceAtLeast(lastNumerator)
    val updatedDenominator = progress.second.coerceAtLeast(lastDenominator)
    if (updatedNumerator > lastNumerator || updatedDenominator > lastDenominator) {
      // Only report progress if it doesn't go backwards (and has actually changed).
      reportProgress(updatedNumerator, updatedDenominator)
    }
    return updatedNumerator to updatedDenominator
  }

  /**
   * The outcome of an attempted Bazel command.
   *
   * Note that the specific possibilities of what the contained [exitCode] can be or whether
   * [outputLines] includes standard error output is dependent on the configuration of the run
   * command.
   *
   * @property exitCode the command's exit code (where '0' is expected to be a success)
   * @property outputLines the list of lines comprising the command's output
   */
  data class Result(val exitCode: Int, val outputLines: List<String>)

  private companion object {
    private const val MAX_ALLOWED_ARG_STR_LENGTH = 50_000

    private val ABSOLUTE_TARGET_PATH_PREFIX_PATTERN = "(?:@[\\w\\-_]+?)?//".toRegex()
    private val CHANGE_TERMINAL_TITLE_PATTERN =
      "^\\x1B]0;\\[([\\d,]+)\\s+/\\s+([\\d,]+)].+?$".toRegex()
    private val ANOTHER_COMMAND_RUNNING_PATTERN =
      "^Another command \\(pid=\\d+?\\) is running. Waiting for it to complete.+?$".toRegex()

    private fun String.parseProgressUpdate(): Pair<Int, Int>? {
      return CHANGE_TERMINAL_TITLE_PATTERN.matchEntire(this)?.let {
        val (numerator, denominator) = it.destructured
        numerator.replace(",", "").toInt() to denominator.replace(",", "").toInt()
      }
    }
  }
}
