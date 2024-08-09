package org.oppia.android.scripts.common

import java.io.File
import java.lang.IllegalArgumentException
import java.util.Locale

/**
 * Utility class to query & interact with a Bazel workspace on the local filesystem (residing within
 * the specified root directory).
 */
class BazelClient(private val rootDirectory: File, private val commandExecutor: CommandExecutor) {
  /** Returns all Bazel test targets in the workspace. */
  fun retrieveAllTestTargets(): List<String> {
    return correctPotentiallyBrokenTargetNames(
      executeBazelCommand("query", "--noshow_progress", "kind(test, //...)")
    )
  }

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
  fun retrieveThirdPartyMavenDepsListForBinary(binaryTarget: String): List<String> {
    return executeBazelCommand(
      "query",
      "deps(deps($binaryTarget) intersect //third_party/...) intersect @maven//..."
    )
  }

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
    )
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
    )
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
      )
    }
  }

  private fun computeMaxArgumentLength(partitions: List<List<String>>) =
    partitions.map(this::computeArgumentLength).maxOrNull() ?: 0

  private fun computeArgumentLength(args: List<String>) = args.joinToString(" ").length

  @Suppress("SameParameterValue") // This check doesn't work correctly for varargs.
  private fun executeBazelCommand(
    vararg arguments: String,
    allowPartialFailures: Boolean = false
  ): List<String> {
    val result =
      commandExecutor.executeCommand(
        rootDirectory, command = "bazel", *arguments, includeErrorOutput = false
      )
    // Per https://docs.bazel.build/versions/main/guide.html#what-exit-code-will-i-get error code of
    // 3 is expected for queries since it indicates that some of the arguments don't correspond to
    // valid targets. Note that this COULD result in legitimate issues being ignored, but it's
    // unlikely.
    val expectedExitCodes = if (allowPartialFailures) listOf(0, 3) else listOf(0)
    check(result.exitCode in expectedExitCodes) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }

  private companion object {
    private const val MAX_ALLOWED_ARG_STR_LENGTH = 50_000
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
