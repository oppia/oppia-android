package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertWithMessage
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import java.io.File
import org.oppia.android.scripts.common.CommandExecutor.OutputRedirectionStrategy.TRACK_AS_OUTPUT

/**
 * Test utility for interacting with a local Git repository on the filesystem, located in the
 * specified [TemporaryFolder]. This is meant to be used to arrange the local test filesystem for
 * use with a real Git application on the host system.
 *
 * Note that constructing this class is insufficient to establish a local Git repository. At
 * minimum, [init] must be called first.
 */
class TestGitRepository(
  private val temporaryRootFolder: TemporaryFolder,
  private val commandExecutorBuilder: CommandExecutor.Builder
) {
  private val rootDirectory by lazy { temporaryRootFolder.root }
  private val commandExecutor by lazy { commandExecutorBuilder.create(rootDirectory) }

  /** Creates the repository using git init. */
  fun init() {
    executeSuccessfulGitCommand("init")
  }

  /** Sets the user's [email] and [name] using git config. */
  fun setUser(email: String, name: String) {
    executeSuccessfulGitCommand("config", "user.email", email)
    executeSuccessfulGitCommand("config", "user.name", name)
  }

  /** Creates a new branch with the specified name, and switches to it. */
  fun checkoutNewBranch(branchName: String) {
    executeSuccessfulGitCommand("checkout", "-b", branchName)
  }

  /**
   * Adds the specified file to be staged for committing (via git add).
   *
   * This does not perform a commit. See [commit] for actually committing the change.
   */
  fun stageFileForCommit(file: File) {
    executeSuccessfulGitCommand("add", file.toRelativeString(rootDirectory))
  }

  /** Stages the iterable of files for commit. See [stageFileForCommit]. */
  fun stageFilesForCommit(files: Iterable<File>) {
    files.forEach(this::stageFileForCommit)
  }

  /**
   * Removes the specified file using git rm, staging it for removal & removing it from the local
   * filesystem.
   *
   * This does not perform a commit. See [commit] for actually committing the change.
   */
  fun removeFileForCommit(file: File) {
    executeSuccessfulGitCommand("rm", file.toRelativeString(rootDirectory))
  }

  /**
   * Moves the [oldFile] to [newFile] using git mv, both performing the move on the local
   * filesystem and staging the move for committing.
   *
   * This does not perform a commit. See [commit] for actually committing the change.
   */
  fun moveFileForCommit(oldFile: File, newFile: File) {
    executeSuccessfulGitCommand(
      "mv",
      oldFile.toRelativeString(rootDirectory),
      newFile.toRelativeString(rootDirectory)
    )
  }

  /**
   * Commits the locally staged files.
   *
   * @param message the message to include as the context for the commit
   * @param allowEmpty whether to allow empty commits (i.e. committing with no staged files)
   */
  fun commit(message: String, allowEmpty: Boolean = false) {
    val arguments = mutableListOf("commit", "-m", message)
    if (allowEmpty) arguments += "--allow-empty"
    executeSuccessfulGitCommand(*arguments.toTypedArray())
  }

  /** Returns the result of git status. */
  fun status(): String {
    return commandExecutor.executeCommandInForeground(
      "git", "status", stderrRedirection = TRACK_AS_OUTPUT
    ).output.joinOutputString()
  }

  private fun executeSuccessfulGitCommand(vararg arguments: String) {
    verifySuccessfulCommand(
      commandExecutor.executeCommandInForeground(
        "git",
        *arguments,
        stderrRedirection = TRACK_AS_OUTPUT)
    )
  }

  private fun verifySuccessfulCommand(result: CommandResult) {
    assertWithMessage("Output: ${result.output.joinOutputString()}")
      .that(result.exitCode)
      .isEqualTo(0)
  }

  private fun List<String>.joinOutputString(): String = joinToString(separator = "\n") { "  $it" }
}
