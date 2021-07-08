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
      val referenceFiles =
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "rbuildfiles($buildFileList)"
        )
      println("@@@@@ Reference build files: $referenceFiles")
      for (file in referenceFiles) {
        val siblingFiles1 =
          executeBazelCommand(
            "query",
            "--noshow_progress",
            "--universe_scope=//...",
            "--order_output=no",
            "kind(test, siblings($file))"
          )
        val siblingFiles2 =
          executeBazelCommand(
            "query",
            "--noshow_progress",
            "--universe_scope=//...",
            "--order_output=no",
            "kind(android_library, siblings($file))"
          )
        println("@@@@@ Sibling files for $file: ${siblingFiles1 + siblingFiles2}")
      }
      val siblingFiles =
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "siblings(rbuildfiles($buildFileList))"
        )
      println("@@@@@ Sibling files: $siblingFiles")
      val rdeps =
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "allrdeps(siblings(rbuildfiles($buildFileList)))"
        )
      println("@@@@@ Sibling rdeps: $rdeps")
      val tests =
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "kind(test, allrdeps(siblings(rbuildfiles($buildFileList))))"
        )
      println("@@@@@ tests: $tests")
      return correctPotentiallyBrokenTargetNames(
        executeBazelCommand(
          "query",
          "--noshow_progress",
          "--universe_scope=//...",
          "--order_output=no",
          "filter('^[^@]', kind(test, allrdeps(siblings(rbuildfiles($buildFileList)))))",
        )
      )
    } else listOf()
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
    println("@@@@@ bazel ${arguments.joinToString(separator = " ")}")
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
