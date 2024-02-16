package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertWithMessage
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import java.io.File

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
  private val commandExecutor: CommandExecutor
) {
  private val rootDirectory by lazy { temporaryRootFolder.root }
  private val gitDirectory: File get() = File(rootDirectory, ".git")
  private val userEmail: String?
    get() = maybeExecuteGitCommand("config", "--local", "--get", "user.email")?.joinOutput()?.trim()
  private val userName: String?
    get() = maybeExecuteGitCommand("config", "--local", "--get", "user.name")?.joinOutput()?.trim()

  /** Creates the repository using git init. */
  fun init() {
    verifyNotInGitRepository()
    executeSuccessfulGitCommand("init")
  }

  /** Sets the user's [email] and [name] using git config. */
  fun setUser(email: String, name: String) {
    verifyInGitRepository()
    verifyUserIsNotSet()
    executeSuccessfulGitCommand("config", "--local", "user.email", email)
    executeSuccessfulGitCommand("config", "--local", "user.name", name)
  }

  /** Creates a new branch with the specified name, and switches to it. */
  fun checkoutNewBranch(branchName: String) {
    verifyInGitRepository()
    executeSuccessfulGitCommand("checkout", "-b", branchName)
  }

  /**
   * Adds the specified file to be staged for committing (via git add).
   *
   * This does not perform a commit. See [commit] for actually committing the change.
   */
  fun stageFileForCommit(file: File) {
    verifyInGitRepository()
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
    verifyInGitRepository()
    executeSuccessfulGitCommand("rm", file.toRelativeString(rootDirectory))
  }

  /**
   * Moves the [oldFile] to [newFile] using git mv, both performing the move on the local
   * filesystem and staging the move for committing.
   *
   * This does not perform a commit. See [commit] for actually committing the change.
   */
  fun moveFileForCommit(oldFile: File, newFile: File) {
    verifyInGitRepository()
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
    verifyInGitRepository()
    verifyUserIsSet()
    val arguments = mutableListOf("commit", "-m", message)
    if (allowEmpty) arguments += "--allow-empty"
    executeSuccessfulGitCommand(*arguments.toTypedArray())
  }

  /** Returns the result of git status. */
  fun status(checkForGitRepository: Boolean = true): String {
    if (checkForGitRepository) verifyInGitRepository()
    return executeGitCommand("status").joinOutput()
  }

  private fun executeGitCommand(vararg arguments: String): CommandResult =
    commandExecutor.executeCommand(rootDirectory, "git", *arguments)

  private fun maybeExecuteGitCommand(vararg arguments: String): CommandResult? =
    executeGitCommand(*arguments).takeIf { it.exitCode == 0 }

  private fun executeSuccessfulGitCommand(vararg arguments: String) =
    verifySuccessfulCommand(executeGitCommand(*arguments))

  private fun verifySuccessfulCommand(result: CommandResult) {
    assertWithMessage("Output: ${result.joinOutput()}")
      .that(result.exitCode)
      .isEqualTo(0)
  }

  private fun verifyInGitRepository() {
    failUnless(gitDirectory.exists()) { "Not operating in an initialized Git repository." }
  }

  private fun verifyNotInGitRepository() {
    failUnless(!gitDirectory.exists()) { "Git repository is already initialized." }
  }

  private fun verifyUserIsNotSet() {
    verifyIsNotSet(name = "User email", userEmail)
    verifyIsNotSet(name = "User name", userName)
  }

  private fun verifyUserIsSet() {
    failUnless(userEmail != null) { "User email has not yet been set." }
    failUnless(userName != null) { "User name has not yet been set." }
  }

  private fun verifyIsNotSet(name: String, value: String?) {
    failUnless(value == null) { "$name has already been set: $value." }
  }

  private fun failUnless(condition: Boolean, lazyMessage: () -> String) {
    if (!condition) throw AssertionError(lazyMessage())
  }

  private fun CommandResult.joinOutput(): String = output.joinToString(separator = "\n") { "  $it" }
}
