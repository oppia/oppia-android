package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.File
import java.lang.IllegalStateException

/**
 * Tests for [GitClient].
 *
 * Note that this test executes real commands on the local filesystem & requires Git in the local
 * environment.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GitClientTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var testGitRepository: TestGitRepository
  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }

  @Before
  fun setUp() {
    testGitRepository = TestGitRepository(tempFolder, commandExecutor)
  }

  @After
  fun tearDown() {
    // Print the status of the git repository to help with debugging in the cases of test failures
    // and to help manually verify the expected git state at the end of each test.
    println("git status (at end of test):")
    println(testGitRepository.status(checkForGitRepository = false))
    scriptBgDispatcher.close()
  }

  @Test
  fun testCurrentCommit_forNonRepository_throwsException() {
    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)

    val exception = assertThrows<IllegalStateException>() { gitClient.currentCommit }

    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().ignoringCase().contains("not a git repository")
  }

  @Test
  fun testCurrentCommit_forValidRepository_returnsCorrectBranch() {
    initializeRepoWithDevelopBranch()
    val developHash = getMostRecentCommitOnCurrentBranch()

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val currentCommit = gitClient.currentCommit

    assertThat(currentCommit).isEqualTo(developHash)
  }

  @Test
  fun testCurrentCommit_switchBranch_returnsCorrectBranch() {
    initializeRepoWithDevelopBranch()
    val developHash = getMostRecentCommitOnCurrentBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    testGitRepository.commit(message = "Test empty commit", allowEmpty = true)
    val featureBranchHash = getMostRecentCommitOnCurrentBranch()

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val currentCommit = gitClient.currentCommit

    assertThat(currentCommit).isNotEqualTo(developHash)
    assertThat(currentCommit).isEqualTo(featureBranchHash)
  }

  @Test
  fun testCurrentBranch_forNonRepository_throwsException() {
    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)

    val exception = assertThrows<IllegalStateException>() { gitClient.currentBranch }

    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().ignoringCase().contains("not a git repository")
  }

  @Test
  fun testCurrentBranch_forValidRepository_returnsCorrectBranch() {
    initializeRepoWithDevelopBranch()

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val currentBranch = gitClient.currentBranch

    assertThat(currentBranch).isEqualTo("develop")
  }

  @Test
  fun testCurrentBranch_switchBranch_returnsCorrectBranch() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val currentBranch = gitClient.currentBranch

    assertThat(currentBranch).isEqualTo("introduce-feature")
  }

  @Test
  fun testBranchMergeBase_forDevelopBranch_returnsLatestCommit() {
    initializeRepoWithDevelopBranch()
    val developHash = getMostRecentCommitOnCurrentBranch()

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val mergeBase = gitClient.branchMergeBase

    assertThat(mergeBase).isEqualTo(developHash)
  }

  @Test
  fun testBranchMergeBase_forFeatureBranch_returnsCorrectHash() {
    initializeRepoWithDevelopBranch()
    val developHash = getMostRecentCommitOnCurrentBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val mergeBase = gitClient.branchMergeBase

    assertThat(mergeBase).isEqualTo(developHash)
  }

  @Test
  fun testBranchMergeBase_forFeatureBranch_withCommit_returnsCorrectHash() {
    initializeRepoWithDevelopBranch()
    val developHash = getMostRecentCommitOnCurrentBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("example_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val mergeBase = gitClient.branchMergeBase

    // The merge base is the latest common hash between this & the develop branch.
    assertThat(mergeBase).isEqualTo(developHash)
  }

  @Test
  fun testChangedFiles_featureBranch_noChangedFiles_isEmpty() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).isEmpty()
  }

  @Test
  fun testChangedFiles_featureBranch_newUntrackedFile_includesFile() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    createNewFile("example_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).containsExactly("example_file")
  }

  @Test
  fun testChangedFiles_featureBranch_stagedFile_includesFile() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    stageNewFile("example_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).containsExactly("example_file")
  }

  @Test
  fun testChangedFiles_featureBranch_committedFile_includesFile() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("example_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).containsExactly("example_file")
  }

  @Test
  fun testChangedFiles_committedFileOnDevelopBranch_switchToFeatureBranch__doesNotIncludeFile() {
    initializeRepoWithDevelopBranch()
    commitNewFile("develop_file")
    testGitRepository.checkoutNewBranch("introduce-feature")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    // Committed files to the develop branch are not included since they aren't part of the feature
    // branch.
    assertThat(changedFiles).isEmpty()
  }

  @Test
  fun testChangedFiles_featureBranch_changedFile_unstaged_includesFile() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("example_file")
    modifyFile("example_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).containsExactly("example_file")
  }

  @Test
  fun testChangedFiles_featureBranch_deletedFile_includesFile() {
    initializeRepoWithDevelopBranch()
    commitNewFile("develop_file")
    testGitRepository.checkoutNewBranch("introduce-feature")
    deleteFile("develop_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).containsExactly("develop_file")
  }

  @Test
  fun testChangedFiles_featureBranch_mixedArrangementOfFiles_includesAllChangedFiles() {
    initializeRepoWithDevelopBranch()
    commitNewFile("develop_branch_file_not_changed")
    commitNewFile("develop_branch_file_changed_not_staged")
    commitNewFile("develop_branch_file_removed")
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("new_feature_file_committed")
    createNewFile("new_untracked_file")
    stageNewFile("new_staged_file")
    modifyFile("develop_branch_file_changed_not_staged")
    deleteFile("develop_branch_file_removed")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val changedFiles = gitClient.changedFiles

    assertThat(changedFiles).containsExactly(
      "develop_branch_file_changed_not_staged", "develop_branch_file_removed",
      "new_feature_file_committed", "new_untracked_file", "new_staged_file"
    )
  }

  @Test
  fun testCommittedFiles_featureBranch_committedFile_includesFile() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("example_file")

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val committedFiles = gitClient.committedFiles

    assertThat(committedFiles).containsExactly("example_file")
  }

  @Test
  fun testCommittedFiles_featureBranch_movedFile_includesFile() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("example_file")
    moveFile(File(tempFolder.root, "example_file"), File(tempFolder.root, "moved_file"))

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val committedFiles = gitClient.committedFiles

    assertThat(committedFiles).containsExactly("moved_file")
  }

  @Test
  fun testCommittedFiles_committedAndMovedFiles_includeAllFiles() {
    initializeRepoWithDevelopBranch()
    testGitRepository.checkoutNewBranch("introduce-feature")
    commitNewFile("committed_file")
    commitNewFile("to_be_moved_file")
    moveFile(File(tempFolder.root, "to_be_moved_file"), File(tempFolder.root, "moved_file"))

    val gitClient = GitClient(tempFolder.root, "develop", commandExecutor)
    val committedAndMovedFiles = gitClient.committedFiles

    assertThat(committedAndMovedFiles).containsExactly("committed_file", "moved_file")
  }

  private fun initializeRepoWithDevelopBranch() {
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.commit(message = "Initial commit.", allowEmpty = true)
  }

  private fun getMostRecentCommitOnCurrentBranch(): String {
    // See https://stackoverflow.com/a/949391 for a reference to validate that this is correct.
    return commandExecutor.executeCommand(
      tempFolder.root, "git", "rev-parse", "HEAD"
    ).output.single()
  }

  private fun createNewFile(name: String): File {
    return tempFolder.newFile(name).let { file ->
      file.writeText("with data")
      file
    }
  }

  private fun stageNewFile(name: String) {
    testGitRepository.stageFileForCommit(createNewFile(name))
  }

  private fun commitNewFile(name: String) {
    stageNewFile(name)
    testGitRepository.commit(message = "Add file $name.")
  }

  private fun moveFile(oldFile: File, newFile: File) {
    testGitRepository.moveFileForCommit(oldFile, newFile)
    testGitRepository.commit(message = "Move from $oldFile to $newFile")
  }

  private fun modifyFile(name: String) {
    File(tempFolder.root, name).appendText("More text")
  }

  private fun deleteFile(name: String) {
    assertThat(File(tempFolder.root, name).delete()).isTrue() // Sanity check.
  }
}
