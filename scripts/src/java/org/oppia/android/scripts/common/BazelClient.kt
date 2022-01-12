package org.oppia.android.scripts.common

import java.io.File
import java.lang.IllegalArgumentException

/**
 * Utility class to query & interact with a Bazel workspace on the local filesystem (residing within
 * the specified root directory).
 */
class BazelClient(
  private val rootDirectory: File,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  /** Returns all Bazel test targets in the workspace. */
  fun retrieveAllTestTargets(): List<String> {
    return correctPotentiallyBrokenTargetNames(
      executeBazelCommand("query", "--noshow_progress", "kind(test, //...)")
    )
  }

  /** Returns all Bazel file targets that correspond to each of the relative file paths provided. */
  fun retrieveBazelTargets(changedFileRelativePaths: Iterable<String>): List<String> {
    return correctPotentiallyBrokenTargetNames(
      executeBazelCommand(
        "query",
        "--noshow_progress",
        "--keep_going",
        "set(${changedFileRelativePaths.joinToString(" ")})",
        allowPartialFailures = true
      )
    )
  }

  /** Returns all test targets in the workspace that are affected by the list of file targets. */
  fun retrieveRelatedTestTargets(fileTargets: Iterable<String>): List<String> {
    return correctPotentiallyBrokenTargetNames(
      executeBazelCommand(
        "query",
        "--noshow_progress",
        "--universe_scope=//...",
        "--order_output=no",
        "kind(test, allrdeps(set(${fileTargets.joinToString(" ")})))"
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
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "rbuildfiles($buildFileList)"
        )
      // Compute only test & library siblings for each individual build file. While this is both
      // much slower than a fully combined query & can potentially miss targets, it runs
      // substantially faster per query and helps to avoid potential hanging in CI.
      val relevantSiblings = referencingBuildFiles.flatMap { buildFileTarget ->
        retrieveFilteredSiblings(filterRuleType = "test", buildFileTarget) +
          retrieveFilteredSiblings(filterRuleType = "android_library", buildFileTarget)
      }.toSet()
      return correctPotentiallyBrokenTargetNames(
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "filter('^[^@]', kind(test, allrdeps(set(${relevantSiblings.joinToString(" ")}))))",
        )
      )
    } else listOf()
  }

  /**
   * Returns the list of direct and indirect production Maven third-party dependencies on which the
   * specified binary depends.
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
