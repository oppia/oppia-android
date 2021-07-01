package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertWithMessage
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.CommandResult
import java.io.File

// TODO: extract to top-level file & document & test
class TestGitWorkspace(private val temporaryRootFolder: TemporaryFolder) {
  private val rootDirectory by lazy { temporaryRootFolder.root }
  private val commandExecutor by lazy { CommandExecutorImpl() }

  fun setUser(email: String, name: String) {
    executeSuccessfulGitCommand("config", "user.email", email)
    executeSuccessfulGitCommand("config", "user.name", name)
  }

  fun init() {
    executeSuccessfulGitCommand("init")
  }

  fun checkoutNewBranch(branchName: String) {
    executeSuccessfulGitCommand("checkout", "-b", branchName)
  }

  fun stageFileForCommit(file: File) {
    executeSuccessfulGitCommand("add", file.toRelativeString(rootDirectory))
  }

  fun stageFilesForCommit(files: Iterable<File>) {
    files.forEach(this::stageFileForCommit)
  }

  fun removeFileForCommit(file: File) {
    executeSuccessfulGitCommand("rm", file.toRelativeString(rootDirectory))
  }

  fun moveFileForCommit(oldFile: File, newFile: File) {
    executeSuccessfulGitCommand(
      "mv",
      oldFile.toRelativeString(rootDirectory),
      newFile.toRelativeString(rootDirectory)
    )
  }

  fun commit(message: String, allowEmpty: Boolean = false) {
    val arguments = mutableListOf("commit", "-m", message)
    if (allowEmpty) arguments += "--allow-empty"
    executeSuccessfulGitCommand(*arguments.toTypedArray())
  }

  fun status(): String {
    return commandExecutor.executeCommand(rootDirectory, "git", "status").output.joinOutputString()
  }

  private fun executeSuccessfulGitCommand(vararg arguments: String) {
    verifySuccessfulCommand(commandExecutor.executeCommand(rootDirectory, "git", *arguments))
  }

  private fun verifySuccessfulCommand(result: CommandResult) {
    assertWithMessage("Output: ${result.output.joinOutputString()}")
      .that(result.exitCode)
      .isEqualTo(0)
  }

  private fun List<String>.joinOutputString(): String = joinToString(separator = "\n") { "  $it" }
}
