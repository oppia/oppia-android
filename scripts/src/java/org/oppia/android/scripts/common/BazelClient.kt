package org.oppia.android.scripts.common

import java.io.File
import java.lang.IllegalArgumentException
import java.util.Locale

/**
 * Utility class to query & interact with a Bazel workspace on the local filesystem (residing within
 * the specified root directory).
 */
class BazelClient(
  private val rootDirectory: File,
  private val commandExecutor: CommandExecutor,
  private val universeScope: String = "//..."
) {
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

  fun sync(): Result = executeBazelCommand("sync")

  fun shutdown(): Result = executeBazelCommand("shutdown")

  fun query(
    pattern: String, withSkyQuery: Boolean = false, allowFailures: Boolean = false
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
    return correctPotentiallyBrokenTargetNames(queryResults.outputLines)
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

  /** Returns all test targets in the workspace that are affected by the list of file targets. */
  fun retrieveRelatedTestTargets(fileTargets: Iterable<String>): List<String> {
    return correctPotentiallyBrokenTargetNames(
      runPotentiallyShardedQueryCommand(
        "kind(test, allrdeps(set(%s)))",
        fileTargets,
        "--noshow_progress",
        "--universe_scope=//...",
        "--order_output=no"
      )
    )
  }

  /**
   * Returns all test targets transitively tied to the specific Bazel BUILD/WORKSPACE files listed
   * in the provided [buildFiles] list. This may return different files than
   * [retrieveRelatedTestTargets] since that method relies on the dependency graph to compute
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
          "--universe_scope=//...",
          "--order_output=no",
          delimiter = ","
        )
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
          "--universe_scope=//...",
          "--order_output=no"
        )
      )
    } else listOf()
  }

  /**
   * Returns the list of direct and indirect Maven third-party dependencies on which the specified
   * binary depends.
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
      "--universe_scope=//...",
      "--order_output=no",
      "kind($filterRuleType, siblings($buildFileTarget))"
    ).outputLines
  }

  private fun correctPotentiallyBrokenTargetNames(lines: List<String>): List<String> {
    val correctedTargets = mutableListOf<String>()
    for (line in lines) {
      when {
        line.isEmpty() -> correctedTargets += line
        else -> {
          val indexes = line.findOccurrencesOf("//")
          if (indexes.isEmpty() || indexes.first() != 0) {
            throw IllegalArgumentException("Invalid line: $line (expected to start with '//')")
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
   * Runs code coverage for the specified Bazel test target.
   *
   * An empty list being returned typically occurs when the coverage command fails to generate any
   * 'coverage.dat' file. This can happen due to tests failures or a misconfiguration that prevents
   * the coverage data from being properly generated.
   *
   * @param bazelTestTarget Bazel test target for which code coverage will be run
   * @return the generated coverage data as a list of list of strings (since there may be more than
   *     one file corresponding to a single test target, e.g. in the case of a sharded test), or an
   *     empty list if no coverage data was found while running the test
   */
  fun runCoverageForTestTarget(bazelTestTarget: String): List<List<String>> {
    val instrumentation = bazelTestTarget.split(":")[0]
    val computeInstrumentation = instrumentation.split("/").let { "//${it[2]}/..." }
    val coverageCommandOutputLines = executeBazelCommand(
      "coverage",
      bazelTestTarget,
      "--instrumentation_filter=$computeInstrumentation"
    ).outputLines
    return parseCoverageDataFilePath(bazelTestTarget, coverageCommandOutputLines).map { path ->
      File(path).readLines()
    }
  }

  private fun parseCoverageDataFilePath(
    bazelTestTarget: String,
    coverageCommandOutputLines: List<String>
  ): List<String> {
    // Use the test target as the base path for the generated coverage.dat file since the test
    // itself may output lines that look like the coverage.dat line (such as in BazelClientTest).
    val targetBasePath = bazelTestTarget.removePrefix("//").replace(':', '/')
    val coverageDatRegex = "^.+?testlogs/$targetBasePath/[^/]*?/?coverage\\.dat$".toRegex()
    return coverageCommandOutputLines.filter(coverageDatRegex::matches).map(String::trim)
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
  ): List<String> {
    // Split up values into partitions to ensure that the argument calls don't over-run the limit.
    var partitionCount = 0
    lateinit var partitions: List<List<String>>
    do {
      partitionCount++
      partitions = values.chunked((values.count() + 1) / partitionCount)
    } while (computeMaxArgumentLength(partitions) >= MAX_ALLOWED_ARG_STR_LENGTH)

    // Fragment the query across the partitions to ensure all values can be considered.
    return partitions.flatMap { partition ->
      val lastArgument = queryFormatStr.format(Locale.US, partition.joinToString(delimiter))
      val allArguments = prefixArgs.toList() + lastArgument
      executeBazelCommand(
        "query", *allArguments.toTypedArray(), allowPartialFailures = allowPartialFailures
      ).outputLines
    }
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
    return Result(result.exitCode, result.output)
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
    line: String, reportProgress: ((Int, Int) -> Unit), lastNumerator: Int, lastDenominator: Int
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

  data class Result(val exitCode: Int, val outputLines: List<String>)

  private companion object {
    private const val MAX_ALLOWED_ARG_STR_LENGTH = 50_000
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

/** Returns a list of indexes where the specified [needle] occurs in this string. */
private fun String.findOccurrencesOf(needle: String): List<Int> {
  val indexes = mutableListOf<Int>()
  var needleIndex = indexOf(needle)
  while (needleIndex >= 0) {
    indexes += needleIndex
    needleIndex = indexOf(needle, startIndex = needleIndex + needle.length)
  }
  return indexes
}
