package org.oppia.android.scripts.common

import java.io.File
import org.oppia.android.scripts.common.CommandExecutor.OutputRedirectionStrategy.TRACK_AS_OUTPUT

/**
 * General utility for interfacing with a Git repository located at the specified working directory
 * and using the specified relative branch reference that should be used when computing changes from
 * the develop branch.
 */
class GitClient(
  private val workingDirectory: File,
  private val baseDevelopBranchReference: String
) {
  private val commandExecutor by lazy {
    CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder().create(workingDirectory)
  }

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
    return executeGitCommandWithOneLineOutput("merge-base $baseDevelopBranchReference HEAD")
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
      commandExecutor.executeCommandInForeground(
        command = "git",
        *argumentsLine.split(" ").toTypedArray(),
        stderrRedirection = TRACK_AS_OUTPUT
      )
    check(result.exitCode == 0) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        " Output:\n${result.output.joinToString("\n")}"
    }
    return result.output
  }
}
