package org.oppia.android.scripts

import java.io.File
import java.lang.IllegalArgumentException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

private const val WAIT_PROCESS_TIMEOUT_MS = 60_000L

/**
 * The main entrypoint for computing the list of affected test targets based on changes in the local
 * Oppia Android Git repository.
 *
 * Usage:
 *   bazel run //scripts:computed_affected_tests -- <path_to_directory_root> <path_to_output_file>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_output_file: path to the file in which the affected test targets will be printed.
 *
 * Example:
 *   bazel run //scripts:computed_affected_tests -- $(pwd) /tmp/affected_tests.log
 */
fun main(args: Array<String>) {
  if (args.size < 2) {
    println(
      "Usage: bazel run //scripts:computed_affected_tests --" +
        " <path_to_directory_root> <path_to_output_file>"
    )
    exitProcess(1)
  }

  val pathToRoot = args[0]
  val pathToOutputFile = args[1]
  val rootDirectory = File(pathToRoot).absoluteFile
  val outputFile = File(pathToOutputFile).absoluteFile

  check(rootDirectory.isDirectory) { "Expected $pathToRoot to be a directory" }
  check(rootDirectory.list().contains("WORKSPACE")) {
    "Expected script to be run from the workspace's root directory"
  }

  println("Running from directory root: $rootDirectory")
  println("Saving results to file: $outputFile")

  val gitClient = GitClient(rootDirectory)
  val bazelClient = BazelClient(rootDirectory)
  println("Current branch: ${gitClient.currentBranch}")
  when (gitClient.currentBranch.toLowerCase(Locale.getDefault())) {
    "develop" -> computeAffectedTargetsForDevelopBranch(bazelClient, outputFile)
    else ->
      computeAffectedTargetsForNonDevelopBranch(gitClient, bazelClient, rootDirectory, outputFile)
  }
}

private fun computeAffectedTargetsForDevelopBranch(bazelClient: BazelClient, outputFile: File) {
  // Compute & print all test targets since this is the develop branch.
  println("Computing all test targets for the develop branch")

  val allTestTargets = bazelClient.retrieveAllTestTargets()
  println()
  println(
    "Affected test targets:" +
      "\n${allTestTargets.joinToString(separator = "\n") { "- $it" }}"
  )
  outputFile.printWriter().use { writer -> allTestTargets.forEach { writer.println(it) } }
}

private fun computeAffectedTargetsForNonDevelopBranch(
  gitClient: GitClient,
  bazelClient: BazelClient,
  rootDirectory: File,
  outputFile: File
) {
  // Compute the list of changed files, but exclude files which no longer exist (since bazel query
  // can't handle these well).
  val changedFiles = gitClient.changedFiles.filter { filepath ->
    File(rootDirectory, filepath).exists()
  }
  println("Changed files (per Git): $changedFiles")

  val changedFileTargets = bazelClient.retrieveBazelTargets(changedFiles).toSet()
  println("Changed Bazel file targets: $changedFileTargets")

  val affectedTestTargets = bazelClient.retrieveRelatedTestTargets(changedFileTargets).toSet()
  println("Affected Bazel test targets: $affectedTestTargets")

  // Compute the list of Bazel files that were changed.
  val changedBazelFiles = changedFiles.filter { file ->
    file.endsWith(".bzl", ignoreCase = true) ||
      file.endsWith(".bazel", ignoreCase = true) ||
      file == "WORKSPACE"
  }
  println("Changed Bazel-specific support files: $changedBazelFiles")

  // Compute the list of affected tests based on BUILD/Bazel/WORKSPACE files. These are generally
  // framed as: if a BUILD file changes, run all tests transitively connected to it.
  val transitiveTestTargets = bazelClient.retrieveTransitiveTestTargets(changedBazelFiles)
  println("Affected test targets due to transitive build deps: $transitiveTestTargets")

  val allAffectedTestTargets = (affectedTestTargets + transitiveTestTargets).toSet()
  println()
  println(
    "Affected test targets:" +
      "\n${allAffectedTestTargets.joinToString(separator = "\n") { "- $it" }}"
  )
  outputFile.printWriter().use { writer -> allAffectedTestTargets.forEach { writer.println(it) } }
}

private class BazelClient(private val rootDirectory: File) {
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
    return correctPotentiallyBrokenTargetNames(
      executeBazelCommand(
        "query",
        "--noshow_progress",
        "--universe_scope=//...",
        "--order_output=no",
        "filter('^[^@]', kind(test, allrdeps(siblings(rbuildfiles(" +
          "${buildFiles.joinToString(",")})))))",
      )
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

  private fun executeBazelCommand(
    vararg arguments: String,
    allowPartialFailures: Boolean = false
  ): List<String> {
    val result =
      executeCommand(rootDirectory, command = "bazel", *arguments, includeErrorOutput = false)
    // Per https://docs.bazel.build/versions/main/guide.html#what-exit-code-will-i-get error code of
    // 3 is expected for queries since it indicates that some of the arguments don't correspond to
    // valid targets. Note that this COULD result in legitimate issues being ignored, but it's
    // unliekly.
    val expectedExitCodes = if (allowPartialFailures) listOf(0, 3) else listOf(0)
    check(result.exitCode in expectedExitCodes) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }
}

private class GitClient(private val workingDirectory: File) {
  /** The name of the current branch of the local Git repository. */
  val currentBranch: String by lazy { retrieveCurrentBranch() }

  /** The hash of of the latest commit common between the current & HEAD branches. */
  val branchMergeBase: String by lazy { retrieveBranchMergeBase() }

  /**
   * The set of files that have been changed in the local branch, including committed, staged,
   * unstaged, and untracked files.
   */
  val changedFiles: Set<String> by lazy { retrieveChangedFilesWithPotentialDuplicates().toSet() }

  private fun retrieveCurrentBranch(): String {
    return executeGitCommandWithOneLineOutput("rev-parse --abbrev-ref HEAD")
  }

  private fun retrieveBranchMergeBase(): String {
    return executeGitCommandWithOneLineOutput("merge-base origin/develop HEAD")
  }

  private fun retrieveChangedFilesWithPotentialDuplicates(): List<String> =
    retrieveChangedCommittedFiles() +
      retrieveChangedStagedFiles() +
      retrieveChangedUnstagedFiles() +
      retrieveChangedUntrackedFiles()

  private fun retrieveChangedCommittedFiles(): List<String> {
    return executeGitCommand("diff --name-only ${computeCommitRange()}")
  }

  private fun retrieveChangedStagedFiles(): List<String> {
    return executeGitCommand("diff --name-only --cached")
  }

  private fun retrieveChangedUnstagedFiles(): List<String> {
    return executeGitCommand("diff --name-only")
  }

  private fun retrieveChangedUntrackedFiles(): List<String> {
    return executeGitCommand("ls-files --others --exclude-standard")
  }

  private fun computeCommitRange(): String = "HEAD..$branchMergeBase"

  private fun executeGitCommandWithOneLineOutput(argumentsLine: String): String {
    val outputLines = executeGitCommand(argumentsLine)
    check(outputLines.size == 1) { "Expected one line of output, not: $outputLines" }
    return outputLines.first()
  }

  private fun executeGitCommand(argumentsLine: String): List<String> {
    val result =
      executeCommand(workingDirectory, command = "git", *argumentsLine.split(" ").toTypedArray())
    check(result.exitCode == 0) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        " Output:\n${result.output.joinToString("\n")}"
    }
    return result.output
  }
}

/**
 * Executes the specified [command] in the specified working directory [workingDir] with the
 * provided arguments being passed as arguments to the command.
 *
 * Any exceptions thrown when trying to execute the application will be thrown by this method.
 * Any failures in the underlying process should not result in an exception.
 *
 * @param includeErrorOutput whether to include error output in the returned [CommandResult],
 *     otherwise it's discarded
 * @return a [CommandResult] that includes the error code & application output
 */
private fun executeCommand(
  workingDir: File,
  command: String,
  vararg arguments: String,
  includeErrorOutput: Boolean = true
): CommandResult {
  check(workingDir.isDirectory) {
    "Expected working directory to be an actual directory: $workingDir"
  }
  val assembledCommand = listOf(command) + arguments.toList()
  val process =
    ProcessBuilder(assembledCommand)
      .directory(workingDir)
      .redirectErrorStream(includeErrorOutput)
      .start()
  val finished = process.waitFor(WAIT_PROCESS_TIMEOUT_MS, TimeUnit.MILLISECONDS)
  check(finished) { "Process did not finish within the expected timeout" }
  return CommandResult(
    process.exitValue(),
    process.inputStream.bufferedReader().readLines(),
    if (!includeErrorOutput) process.errorStream.bufferedReader().readLines() else listOf(),
    assembledCommand,
  )
}

/** The result of executing a command using [executeCommand]. */
private data class CommandResult(
  /** The exit code of the application. */
  val exitCode: Int,
  /** The lines of output from the command, including both error & standard output lines. */
  val output: List<String>,
  /** The lines of error output, or empty if error output is redirected to [output]. */
  val errorOutput: List<String>,
  /** The fully-formed command line executed by the application to achieve this result. */
  val command: List<String>,
)

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
