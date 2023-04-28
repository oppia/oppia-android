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
    pattern: String,
    keepGoing: Boolean = false,
    allowFailures: Boolean = false,
    checkUpToDate: Boolean = false,
    buildRunfileLinks: Boolean = true
  ): List<String> {
    return executeBazelCommand(
      *listOfNotNull(
        "build",
        "--noshow_progress",
        "--keep_going".takeIf { keepGoing },
        "--check_up_to_date".takeIf { checkUpToDate },
        "--nobuild_runfile_links".takeUnless { buildRunfileLinks },
        pattern,
      ).toTypedArray(),
      allowAllFailures = allowFailures
    )
  }

  fun query(
    pattern: String, withSkyQuery: Boolean = false, allowFailures: Boolean = false
  ): List<String> {
    val args = listOfNotNull(
      "query",
      "--noshow_progress",
      "--order_output=no".takeIf { withSkyQuery },
      "--universe_scope=$universeScope".takeIf { withSkyQuery },
      pattern
    )
    // Ignore queries which result in an error if allowFailures is enabled.
    val queryResults =
      executeBazelCommand(
        *args.toTypedArray(), allowAllFailures = allowFailures, includeErrorOutput = false
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
    )
  }

  private fun correctPotentiallyBrokenTargetNames(lines: List<String>): List<String> {
    val correctedTargets = mutableListOf<String>()
    for (line in lines) {
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
    partitions.maxOfOrNull(this::computeArgumentLength) ?: 0

  private fun computeArgumentLength(args: List<String>) = args.joinToString(" ").length

  @Suppress("SameParameterValue") // This check doesn't work correctly for varargs.
  private fun executeBazelCommand(
    vararg arguments: String,
    allowAllFailures: Boolean = false,
    allowPartialFailures: Boolean = false,
    includeErrorOutput: Boolean = allowAllFailures
  ): List<String> {
    val result =
      commandExecutor.executeCommand(
        rootDirectory, command = "bazel", *arguments, includeErrorOutput = includeErrorOutput
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
    return result.output
  }

  private companion object {
    private const val MAX_ALLOWED_ARG_STR_LENGTH = 50_000

    private val ABSOLUTE_TARGET_PATH_PREFIX_PATTERN = "(?:@[\\w\\-_]+?)?//".toRegex()
  }
}
