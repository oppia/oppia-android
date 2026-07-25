package org.oppia.android.scripts.common

import java.io.File

/**
 * General utility for interfacing with a Git repository located at the specified working directory
 * and using the specified base commit hash reference that should be used when computing changes
 * from the local branch.
 */
class GitClient(
  private val workingDirectory: File,
  private val baseCommit: String,
  private val commandExecutor: CommandExecutor
) {
  /** The commit hash of the HEAD of the local Git repository. */
  val currentCommit: String by lazy { retrieveCurrentCommit() }

  /** The name of the current branch of the local Git repository. */
  val currentBranch: String by lazy { retrieveCurrentBranch() }

  /** The hash of of the latest commit common between the current & HEAD branches. */
  val branchMergeBase: String by lazy { retrieveBranchMergeBase() }

  /** Whether the local Git repository is a shallow clone. */
  val isShallowRepository: Boolean by lazy { retrieveIsShallowRepository() }

  private val resolvedBaseCommit: String by lazy {
    executeGitCommandWithOneLineOutput("rev-parse $baseCommit")
  }

  /**
   * The set of files that have been changed in the local branch, including committed, staged,
   * unstaged, and untracked files.
   */
  val changedFiles: Set<String> by lazy { retrieveChangedFilesWithPotentialDuplicates().toSet() }

  /** The list of files that have been committed in the local branch. */
  val committedFiles: List<String> by lazy {
    retrieveChangedCommittedFiles() +
      retrieveRenamedFiles()
  }

  /** Returns the number of commits for the specified revision reference. */
  fun countCommits(revision: String): Int {
    return executeGitCommandWithOneLineOutput("rev-list --count $revision").toIntOrNull()
      ?: error("Failed to parse commit count for: $revision")
  }

  private fun retrieveCurrentCommit(): String {
    return executeGitCommandWithOneLineOutput("rev-parse HEAD")
  }

  private fun retrieveCurrentBranch(): String {
    return executeGitCommandWithOneLineOutput("rev-parse --abbrev-ref HEAD")
  }

  private fun retrieveBranchMergeBase(): String {
    return executeGitCommandWithOneLineOutput("merge-base $resolvedBaseCommit HEAD").also {
      if (resolvedBaseCommit != it) {
        println("WARNING: Provided base commit $resolvedBaseCommit doesn't match merge-base: $it.")
      }
    }
  }

  private fun retrieveIsShallowRepository(): Boolean {
    return executeGitCommandWithOneLineOutput("rev-parse --is-shallow-repository").toBooleanStrict()
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

  private fun retrieveRenamedFiles(): List<String> {
    val renamedFilesCommand = executeGitCommand("diff -M --name-status ${computeCommitRange()}")
    return renamedFilesCommand.filter { it.startsWith("R") }
      .map { it.split("\t")[1] }
  }

  private fun computeCommitRange(): String = "HEAD..$branchMergeBase"

  private fun executeGitCommandWithOneLineOutput(argumentsLine: String): String {
    val outputLines = executeGitCommand(argumentsLine)
    check(outputLines.size == 1) { "Expected one line of output, not: $outputLines" }
    return outputLines.first()
  }

  private fun executeGitCommand(argumentsLine: String): List<String> {
    val result =
      commandExecutor.executeCommand(
        workingDirectory, command = "git", *argumentsLine.split(" ").toTypedArray()
      )
    check(result.exitCode == 0) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        " Output:\n${result.output.joinToString("\n")}"
    }
    return result.output
  }
}
