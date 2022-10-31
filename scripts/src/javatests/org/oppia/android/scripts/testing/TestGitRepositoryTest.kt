package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.CommandResult
import org.oppia.android.testing.assertThrows
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.oppia.android.scripts.common.CommandExecutor.OutputRedirectionStrategy
import org.oppia.android.scripts.common.CommandExecutor.OutputRedirectionStrategy.TRACK_AS_OUTPUT

/**
 * Tests for [TestGitRepository].
 *
 * Note that this test suite operates similarly to the one for TestBazelWorkspace in that it relies
 * on how it's used with other tests to ensure correctness. However, it also utilizes trivial and
 * well understood Git commands to ensure the utility is performing the operations as expected. This
 * does result in the suite largely testing Git itself for an otherwise simple utility. While true,
 * this suite is meant to ensure the contract of [TestGitRepository] is enforced and that utility
 * behaves exactly in the way guaranteed per this test suite.
 *
 * Finally, as indicated above, this test depends both on a real filesystem and requires the
 * presence of Git in the user's local environment. One consequence is that since Git's version is
 * not configurable at the project level, subtle differences in different Git installations or
 * inconsistencies across versions could result in failures in this test. The team will refine these
 * tests over time to try and be as broadly inclusive for different Git clients/versions as
 * possible.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TestGitRepositoryTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private val commandExecutorBuilderInterceptor by lazy { CommandExecutorBuilderInterceptor() }

  @Test
  fun testCreateTestUtility_doesNotImmediatelyCreateAnyFiles() {
    TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)

    // Simply creating the utility should not create any files. This ensures later tests are
    // beginning in a sane state.
    assertThat(tempFolder.root.listFiles()).isEmpty()
  }

  @Test
  fun testInit_newDirectory_initializesRepository() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)

    testGitRepository.init()

    assertThat(tempFolder.root?.list()?.toList()).containsExactly(".git")
  }

  @Test
  fun testSetUser_noGitRepository_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)

    val error = assertThrows(AssertionError::class) {
      testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    }

    assertThat(error).hasMessageThat().contains("not in a git directory")
  }

  @Test
  fun testSetUser_validGitRepository_setsCorrectEmail() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()

    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")

    val email = executeGitCommand("config", "user.email").getOnlyOutputLine()
    assertThat(email).isEqualTo("test@oppia.org")
  }

  @Test
  fun testSetUser_validGitRepository_setsCorrectName() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()

    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")

    val email = executeGitCommand("config", "user.name").getOnlyOutputLine()
    assertThat(email).isEqualTo("Test User")
  }

  @Test
  fun testCheckOutNewBranch_notGitRepository_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)

    val error = assertThrows(AssertionError::class) {
      testGitRepository.checkoutNewBranch("develop")
    }

    assertThat(error).hasMessageThat().ignoringCase().contains("not a git repository")
  }

  @Test
  fun testCheckOutNewBranch_validGitRepository_newBranch_createsAndSwitchesToBranch() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()

    testGitRepository.checkoutNewBranch("develop")

    val lastExecutor = commandExecutorBuilderInterceptor.getLastCommandExecutor()
    val output = lastExecutor.getLastCommandResult().getOutputAsJoinedString()
    assertThat(output).contains("Switched to a new branch")
  }

  @Test
  fun testStageFileForCommit_nonexistentFile_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.stageFileForCommit(File(tempFolder.root, "fake_file"))
    }

    assertThat(error).hasMessageThat().contains("did not match any files")
  }

  @Test
  fun testStageFileForCommit_newFile_stagesFileForAdding() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    testGitRepository.stageFileForCommit(tempFolder.newFile("example_file"))

    val status = executeGitCommand("status").getOutputAsJoinedString()
    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("new file:.+?example_file")
  }

  @Test
  fun testStageFilesForCommit_emptyList_doesNothing() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    testGitRepository.stageFilesForCommit(listOf())

    val status = executeGitCommand("status").getOutputAsJoinedString()
    assertThat(status).contains("nothing to commit")
  }

  @Test
  fun testStageFilesForCommit_oneNewFile_stagesFile() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    testGitRepository.stageFilesForCommit(listOf(tempFolder.newFile("example_file")))

    val status = executeGitCommand("status").getOutputAsJoinedString()
    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("new file:.+?example_file")
  }

  @Test
  fun testStageFilesForCommit_multipleFiles_stagesFilesForAdding() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    testGitRepository.stageFilesForCommit(
      listOf(
        tempFolder.newFile("new_file1"),
        tempFolder.newFile("new_file2"),
        tempFolder.newFile("new_file3")
      )
    )

    val status = executeGitCommand("status").getOutputAsJoinedString()
    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("new file:.+?new_file1")
    assertThat(status).containsMatch("new file:.+?new_file2")
    assertThat(status).containsMatch("new file:.+?new_file3")
  }

  @Test
  fun testStageFilesForCommit_multipleFiles_oneDoesNotExist_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.stageFilesForCommit(
        listOf(
          tempFolder.newFile("new_file1"),
          File(tempFolder.root, "nonexistent_file"),
          tempFolder.newFile("new_file2")
        )
      )
    }

    assertThat(error).hasMessageThat().contains("did not match any files")
  }

  @Test
  fun testRemoveFileForCommit_nonexistentFile_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.removeFileForCommit(File(tempFolder.root, "nonexistent_file"))
    }

    assertThat(error).hasMessageThat().contains("did not match any files")
  }

  @Test
  fun testRemoveFileForCommit_untrackedFile_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    tempFolder.newFile("untracked_file")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.removeFileForCommit(File(tempFolder.root, "untracked_file"))
    }

    assertThat(error).hasMessageThat().contains("did not match any files")
  }

  @Test
  fun testRemoveFileForCommit_committedFile_stagesFileForRemovalAndRemovesFileFromFilesystem() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("committed_file"))
    testGitRepository.commit("Commit new file.")

    testGitRepository.removeFileForCommit(File(tempFolder.root, "committed_file"))

    val status = executeGitCommand("status").getOutputAsJoinedString()
    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("deleted:.+?committed_file")
  }

  @Test
  fun testMoveFileForCommit_oldFileDoesNotExist_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.moveFileForCommit(
        File(tempFolder.root, "nonexistent_file"), File(tempFolder.root, "new_file")
      )
    }

    assertThat(error).hasMessageThat().contains("bad source")
  }

  @Test
  fun testMoveFileForCommit_oldFileIsUntracked_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    tempFolder.newFile("untracked_file")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.moveFileForCommit(
        File(tempFolder.root, "untracked_file"), File(tempFolder.root, "new_file")
      )
    }

    assertThat(error).hasMessageThat().contains("not under version control")
  }

  @Test
  fun testMoveFileForCommit_oldFileCommitted_createsNewFileAndStagesItForMove() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("committed_file"))
    testGitRepository.commit("Commit new file.")

    testGitRepository.moveFileForCommit(
      File(tempFolder.root, "committed_file"), File(tempFolder.root, "moved_file")
    )

    // Verify that the moved file was actually moved, and verify via Git status.
    assertThat(File(tempFolder.root, "committed_file").exists()).isFalse()
    assertThat(File(tempFolder.root, "moved_file").exists()).isTrue()

    val status = executeGitCommand("status").getOutputAsJoinedString()
    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("renamed:.+?committed_file.+?moved_file")
  }

  @Test
  fun testCommit_noUser_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("file_to_be_committed"))

    val error = assertThrows(AssertionError::class) {
      testGitRepository.commit("Commit new file.")
    }

    assertThat(error).hasMessageThat().contains("Please tell me who you are")
  }

  @Test
  fun testCommit_doNotAllowEmptyCommit_nothingStaged_throwsAssertionError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")

    val error = assertThrows(AssertionError::class) {
      testGitRepository.commit("Attempting empty commit.", allowEmpty = false)
    }

    assertThat(error).hasMessageThat().contains("nothing to commit")
  }

  @Test
  fun testCommit_allowEmptyCommit_nothingStaged_createsEmptyCommitWithMessage() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")

    testGitRepository.commit("Attempting empty commit.", allowEmpty = true)

    val log = executeGitCommand("log").getOutputAsJoinedString()
    assertThat(log).contains("Attempting empty commit.")
  }

  @Test
  fun testCommit_filesStaged_createsCommitWithMessage() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.stageFileForCommit(tempFolder.newFile("file_to_commit"))

    testGitRepository.commit("Committing new file.")

    // Verify that the file was committed & that the commit exists.
    val status = executeGitCommand("status").getOutputAsJoinedString()
    val log = executeGitCommand("log").getOutputAsJoinedString()
    assertThat(status).contains("nothing to commit")
    assertThat(log).contains("Committing new file.")
    assertThat(File(tempFolder.root, "file_to_commit").exists()).isTrue()
  }

  @Test
  fun testStatus_noGitRepository_hasStatusWithError() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)

    val status = testGitRepository.status()

    assertThat(status).ignoringCase().contains("not a git repository")
  }

  @Test
  fun testStatus_onBranch_nothingStaged_statusEmpty() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()

    val status = testGitRepository.status()

    assertThat(status).contains("nothing to commit")
  }

  @Test
  fun testStatus_afterStageFileForAdd_statusIncludesStagedFile() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("staged_file"))

    val status = testGitRepository.status()

    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("new file:.+?staged_file")
  }

  @Test
  fun testStatus_afterStageFileForDelete_statusIncludesStagedFile() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("committed_file"))
    testGitRepository.commit("Commit new file.")
    testGitRepository.removeFileForCommit(File(tempFolder.root, "committed_file"))

    val status = testGitRepository.status()

    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("deleted:.+?committed_file")
  }

  @Test
  fun testStatus_afterStageFileForMove_statusIncludesFileForMove() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("committed_file"))
    testGitRepository.commit("Commit new file.")
    testGitRepository.moveFileForCommit(
      File(tempFolder.root, "committed_file"), File(tempFolder.root, "moved_file")
    )

    val status = testGitRepository.status()

    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("renamed:.+?committed_file.+?moved_file")
  }

  @Test
  fun testStatus_multipleFilesStaged_statusIncludesAll() {
    // Note that the test files in this test require content so that Git doesn't think the
    // delete/add is a move.
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(generateFileWithRandomContent("file_to_remove"))
    testGitRepository.stageFileForCommit(generateFileWithRandomContent("committed_file"))
    testGitRepository.commit("Commit new files.")
    testGitRepository.stageFileForCommit(generateFileWithRandomContent("staged_file"))
    testGitRepository.removeFileForCommit(File(tempFolder.root, "file_to_remove"))
    testGitRepository.moveFileForCommit(
      File(tempFolder.root, "committed_file"), File(tempFolder.root, "moved_file")
    )

    val status = testGitRepository.status()

    assertThat(status).contains("Changes to be committed")
    assertThat(status).containsMatch("new file:.+?staged_file")
    assertThat(status).containsMatch("renamed:.+?committed_file.+?moved_file")
    assertThat(status).containsMatch("deleted:.+?file_to_remove")
  }

  @Test
  fun testStatus_multipleFilesStaged_committed_statusIsEmpty() {
    val testGitRepository = TestGitRepository(tempFolder, commandExecutorBuilderInterceptor)
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFileForCommit(tempFolder.newFile("committed_file1"))
    testGitRepository.stageFileForCommit(tempFolder.newFile("committed_file2"))
    testGitRepository.commit("Commit new files.")

    val status = testGitRepository.status()

    assertThat(status).contains("nothing to commit")
  }

  private fun executeGitCommand(vararg args: String): CommandResult {
    val lastExecutor = commandExecutorBuilderInterceptor.getLastCommandExecutor()
    return lastExecutor.executeCommandInForeground(
      "git", *args, stderrRedirection = TRACK_AS_OUTPUT
    )
  }

  private fun CommandResult.getOnlyOutputLine(): String = output.single()

  private fun CommandResult.getOutputAsJoinedString(): String =
    output.joinToString(separator = "\n")

  private fun generateFileWithRandomContent(name: String): File {
    val file = tempFolder.newFile(name)
    file.writeText(UUID.randomUUID().toString())
    return file
  }

  private class CommandExecutorInterceptor(
    private val realCommandExecutor: CommandExecutor
  ) : CommandExecutor {
    private val commandResults = mutableListOf<CommandResult>()

    override fun executeCommandInForeground(
      command: String,
      vararg arguments: String,
      stdoutRedirection: OutputRedirectionStrategy,
      stderrRedirection: OutputRedirectionStrategy
    ): CommandResult {
      val result =
        realCommandExecutor.executeCommandInForeground(
          command,
          *arguments,
          stdoutRedirection = stdoutRedirection,
          stderrRedirection = stderrRedirection
        )
      commandResults += result
      return result
    }

    /**
     * Returns the [CommandResult] of the most recent command executed by this executor, or throws
     * an exception if none have yet been executed.
     */
    fun getLastCommandResult(): CommandResult = commandResults.last()

    override fun executeCommandInBackgroundAsync(
      command: String, vararg arguments: String
    ) = error("Running commands in the background is not supported in this executor.")
  }

  private class CommandExecutorBuilderInterceptor: CommandExecutor.Builder {
    private val realBuilder by lazy {
      CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder()
    }
    private val commandExecutorInterceptors = mutableListOf<CommandExecutorInterceptor>()

    override fun setEnvironmentVariable(name: String, value: String): CommandExecutor.Builder {
      realBuilder.setEnvironmentVariable(name, value)
      return this
    }

    override fun setProcessTimeout(timeout: Long, timeoutUnit: TimeUnit): CommandExecutor.Builder {
      realBuilder.setProcessTimeout(timeout, timeoutUnit)
      return this
    }

    override fun create(workingDirectory: File): CommandExecutor {
      val executor = CommandExecutorInterceptor(realBuilder.create(workingDirectory))
      commandExecutorInterceptors += executor
      return executor
    }

    fun getLastCommandExecutor(): CommandExecutorInterceptor = commandExecutorInterceptors.last()
  }
}
