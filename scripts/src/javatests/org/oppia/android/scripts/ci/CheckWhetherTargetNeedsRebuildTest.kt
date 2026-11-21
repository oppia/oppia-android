package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/**
 * Tests for the check_whether_target_needs_rebuild utility.
 *
 * Note that this test suite makes use of real Bazel & Git utilities on the local system. As a
 * result, these tests could be affected by unexpected environment issues (such as inconsistencies
 * across dependency versions or changes in behavior across different filesystems).
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class CheckWhetherTargetNeedsRebuildTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy {
    CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }

  private lateinit var testGitRepository: TestGitRepository
  private lateinit var pendingOutputStream: ByteArrayOutputStream
  private lateinit var originalStandardOutputStream: PrintStream

  @Before
  fun setUp() {
    testGitRepository = TestGitRepository(tempFolder, commandExecutor)

    // Redirect script output for testing purposes.
    pendingOutputStream = ByteArrayOutputStream()
    originalStandardOutputStream = System.out
    System.setOut(PrintStream(pendingOutputStream))
  }

  @After
  fun tearDown() {
    System.setOut(originalStandardOutputStream)
    scriptBgDispatcher.close()
  }

  @Test
  fun testUtility_noArguments_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { main(arrayOf()) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_twoArguments_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { main(arrayOf("first", "second")) }

    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_directoryRootDoesNotExist_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      checkNeedsRebuild(pathToRoot = "fake")
    }

    assertThat(exception).hasMessageThat().contains("Expected 'fake' to be a directory")
  }

  @Test
  fun testUtility_directoryRootIsNotWorkspace_throwsException() {
    val exception = assertThrows<IllegalStateException>() { checkNeedsRebuild() }

    assertThat(exception).hasMessageThat()
      .contains("Expected script to be run from the workspace's root directory")
  }

  @Test
  fun testUtility_onDevelopBranch_needsRebuild() {
    initializeWorkspaceOnDevelop()

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("always rebuilt on develop")
  }

  @Test
  fun testUtility_featureBranch_noChanges_doesNotNeedRebuild() {
    initializeWorkspaceOnFeatureBranch()

    assertThat(checkNeedsRebuild()).isFalse()
  }

  @Test
  fun testUtility_featureBranch_changedUnrelatedFile_doesNotNeedRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("README.md")

    assertThat(checkNeedsRebuild()).isFalse()
  }

  @Test
  fun testUtility_featureBranch_changedTargetSource_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("app/App.txt")

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("[app/App.txt]")
  }

  @Test
  fun testUtility_featureBranch_changedTransitiveDependencySource_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("lib/Lib.txt")

    assertThat(checkNeedsRebuild()).isTrue()
  }

  @Test
  fun testUtility_featureBranch_changedOtherTargetSource_doesNotNeedRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("other/Other.txt")

    assertThat(checkNeedsRebuild()).isFalse()
  }

  @Test
  fun testUtility_featureBranch_changedOtherTargetSource_otherTargetNeedsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("other/Other.txt")

    assertThat(checkNeedsRebuild(bazelTarget = "//other:other")).isTrue()
  }

  @Test
  fun testUtility_featureBranch_changedTargetBuildFile_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("lib/BUILD.bazel")

    assertThat(checkNeedsRebuild()).isTrue()
  }

  @Test
  fun testUtility_featureBranch_changedOtherTargetBuildFile_doesNotNeedRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("other/BUILD.bazel")

    assertThat(checkNeedsRebuild()).isFalse()
  }

  @Test
  fun testUtility_featureBranch_changedBzlFile_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("other/unused.bzl")

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("build-wide files: [other/unused.bzl]")
  }

  @Test
  fun testUtility_featureBranch_changedPatchFile_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("other/fix.patch")

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("build-wide files: [other/fix.patch]")
  }

  @Test
  fun testUtility_featureBranch_changedWorkspace_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("WORKSPACE")

    assertThat(checkNeedsRebuild()).isTrue()
  }

  @Test
  fun testUtility_featureBranch_changedBuildWorkflow_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile(".github/workflows/build_tests.yml")

    assertThat(checkNeedsRebuild()).isTrue()
  }

  @Test
  fun testUtility_featureBranch_uncommittedTargetSourceChange_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeFile("app/App.txt")

    assertThat(checkNeedsRebuild()).isTrue()
  }

  @Test
  fun testUtility_featureBranch_newGlobMatchedTargetSource_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    changeAndCommitFile("app/NewApp.txt")

    assertThat(checkNeedsRebuild()).isTrue()
  }

  @Test
  fun testUtility_featureBranch_deletedGlobMatchedTargetSource_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    deleteAndCommitFile("app/Extra.txt")

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("[app/Extra.txt]")
  }

  @Test
  fun testUtility_featureBranch_movedFileIntoTargetDirectory_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    moveAndCommitFile("docs/Guide.md", "app/Guide.txt")

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("app/Guide.txt")
  }

  @Test
  fun testUtility_featureBranch_movedFileOutOfTargetDirectory_needsRebuild() {
    initializeWorkspaceOnFeatureBranch()

    moveAndCommitFile("app/Extra.txt", "docs/Extra.txt")

    assertThat(checkNeedsRebuild()).isTrue()
    assertThat(pendingOutputStream.toString()).contains("app/Extra.txt")
  }

  @Test
  fun testUtility_featureBranch_movedUnrelatedFile_doesNotNeedRebuild() {
    initializeWorkspaceOnFeatureBranch()

    moveAndCommitFile("docs/Guide.md", "docs/NewGuide.md")

    assertThat(checkNeedsRebuild()).isFalse()
  }

  @Test
  fun testUtility_featureBranch_deletedUnrelatedFile_doesNotNeedRebuild() {
    initializeWorkspaceOnFeatureBranch()

    deleteAndCommitFile("docs/Guide.md")

    assertThat(checkNeedsRebuild()).isFalse()
  }

  private fun checkNeedsRebuild(
    pathToRoot: String = tempFolder.root.absolutePath,
    bazelTarget: String = "//app:app"
  ): Boolean {
    return CheckWhetherTargetNeedsRebuild(scriptBgDispatcher, commandExecutor)
      .needsRebuild(pathToRoot, baseCommit = "develop", bazelTarget)
  }

  /**
   * Initializes a workspace with an '//app:app' target (which depends on '//lib:lib') and an
   * unrelated '//other:other' target, and commits it to a new 'develop' branch.
   */
  private fun initializeWorkspaceOnDevelop() {
    TestBazelWorkspace(tempFolder).initEmptyWorkspace()
    writeFile(
      "app/BUILD.bazel",
      """
      filegroup(name = "app", srcs = glob(["*.txt"]) + ["//lib:lib"])
      """
    )
    writeFile("app/App.txt")
    writeFile("app/Extra.txt")
    writeFile(
      "lib/BUILD.bazel",
      """
      filegroup(name = "lib", srcs = ["Lib.txt"], visibility = ["//visibility:public"])
      """
    )
    writeFile("lib/Lib.txt")
    writeFile(
      "other/BUILD.bazel",
      """
      filegroup(name = "other", srcs = ["Other.txt"])
      """
    )
    writeFile("other/Other.txt")
    writeFile("other/unused.bzl")
    writeFile("other/fix.patch")
    writeFile(".github/workflows/build_tests.yml")
    writeFile("README.md")
    writeFile("docs/Guide.md")
    val workspaceFiles = tempFolder.root.walk().filter { it.isFile }.toList()

    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.stageFilesForCommit(workspaceFiles)
    testGitRepository.commit(message = "Initial commit.")
  }

  private fun initializeWorkspaceOnFeatureBranch() {
    initializeWorkspaceOnDevelop()
    testGitRepository.checkoutNewBranch("introduce-feature")
  }

  private fun writeFile(relativePath: String, content: String = "") {
    File(tempFolder.root, relativePath).apply {
      parentFile?.mkdirs()
      writeText(content.trimIndent())
    }
  }

  private fun changeFile(relativePath: String): File {
    return File(tempFolder.root, relativePath).apply { appendText("\n# Changed.\n") }
  }

  private fun changeAndCommitFile(relativePath: String) {
    testGitRepository.stageFileForCommit(changeFile(relativePath))
    testGitRepository.commit(message = "Change $relativePath.")
  }

  private fun moveAndCommitFile(oldRelativePath: String, newRelativePath: String) {
    testGitRepository.moveFileForCommit(
      File(tempFolder.root, oldRelativePath), File(tempFolder.root, newRelativePath)
    )
    testGitRepository.commit(message = "Move $oldRelativePath to $newRelativePath.")
  }

  private fun deleteAndCommitFile(relativePath: String) {
    testGitRepository.removeFileForCommit(File(tempFolder.root, relativePath))
    testGitRepository.commit(message = "Delete $relativePath.")
  }
}
