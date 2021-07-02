package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

/**
 * Tests for the compute_affected_tests utility.
 *
 * Note that this test suite makes use of real Bazel & Git utilities on the local system. As a
 * result, these tests could be affected by unexpected environment issues (such as inconsistencies
 * across dependency versions or changes in behavior across different filesystems).
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class ComputeAffectedTestsTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var testGitRepository: TestGitRepository

  private lateinit var pendingOutputStream: ByteArrayOutputStream
  private lateinit var originalStandardOutputStream: OutputStream

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testGitRepository = TestGitRepository(tempFolder)

    // Redirect script output for testing purposes.
    pendingOutputStream = ByteArrayOutputStream()
    originalStandardOutputStream = System.out
    System.setOut(PrintStream(pendingOutputStream))
  }

  @After
  fun tearDown() {
    // Reinstate test output redirection.
    System.setOut(PrintStream(pendingOutputStream))

    // Print the status of the git repository to help with debugging in the cases of test failures
    // and to help manually verify the expect git state at the end of each test.
    println("git status (at end of test):")
    println(testGitRepository.status())
  }

  @Test
  fun testUtility_noArguments_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) { main(arrayOf()) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_oneArgument_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) { main(arrayOf("first")) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_directoryRootDoesNotExist_throwsException() {
    val exception = assertThrows(IllegalStateException::class) { main(arrayOf("fake", "alsofake")) }

    assertThat(exception).hasMessageThat().contains("Expected 'fake' to be a directory")
  }

  @Test
  fun testUtility_emptyDirectory_throwsException() {
    val exception = assertThrows(IllegalStateException::class) { runScript() }

    assertThat(exception).hasMessageThat().contains("run from the workspace's root directory")
  }

  @Test
  fun testUtility_emptyWorkspace_returnsNoTargets() {
    // Need to be on a feature branch since the develop branch expects there to be targets.
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createEmptyWorkspace()

    val reportedTargets = runScript()

    // An empty workspace should yield no targets.
    assertThat(reportedTargets).isEmpty()
  }

  @Test
  fun testUtility_bazelWorkspace_developBranch_returnsAllTests() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")

    val reportedTargets = runScript()

    // Since the develop branch is checked out, all test targets should be returned.
    assertThat(reportedTargets).containsExactly("//:FirstTest", "//:SecondTest", "//:ThirdTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_noChanges_returnsNoTargets() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()

    val reportedTargets = runScript()

    // No changes are on the feature branch, so no targets should be returned.
    assertThat(reportedTargets).isEmpty()
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_testChange_committed_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeAndCommitTestFile("FirstTest")

    val reportedTargets = runScript()

    // Only the first test should be reported since the test file itself was changed & committed.
    assertThat(reportedTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_testChange_staged_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeAndStageTestFile("FirstTest")

    val reportedTargets = runScript()

    // Only the first test should be reported since the test file itself was changed & staged.
    assertThat(reportedTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_testChange_unstaged_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeTestFile("FirstTest")

    val reportedTargets = runScript()

    // The first test should still be reported since it was changed (even though it wasn't staged).
    assertThat(reportedTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_newTest_untracked_returnsNewTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    // A separate subpackage is needed to avoid unintentionally changing the BUILD file used by the
    // other already-committed tests.
    createBasicTests("NewUntrackedTest", subpackage = "newtest")

    val reportedTargets = runScript()

    // The new test should still be reported since it was changed (even though it wasn't staged).
    assertThat(reportedTargets).containsExactly("//newtest:NewUntrackedTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_dependencyChange_committed_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", withGeneratedDependencies = true)
    switchToFeatureBranch()
    changeAndCommitDependencyFileForTest("FirstTest")

    val reportedTargets = runScript()

    // The first test should be reported since its dependency was changed.
    assertThat(reportedTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_commonDepChange_committed_returnsTestTargets() {
    initializeEmptyGitRepository()
    val targetName = createAndCommitLibrary("CommonDependency")
    createAndCommitBasicTests("FirstTest", withGeneratedDependencies = true)
    createAndCommitBasicTests("SecondTest", "ThirdTest", withExtraDependency = targetName)
    switchToFeatureBranch()
    changeAndCommitLibrary("CommonDependency")

    val reportedTargets = runScript()

    // The two tests with a common dependency should be reported since that dependency was changed.
    assertThat(reportedTargets).containsExactly("//:SecondTest", "//:ThirdTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_buildFileChange_committed_returnsRelatedTargets() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest")
    switchToFeatureBranch()
    createAndCommitBasicTests("ThirdTest")

    val reportedTargets = runScript()

    // Introducing a fourth test requires changing the common BUILD file which leads to the other
    // tests becoming affected.
    assertThat(reportedTargets).containsExactly("//:FirstTest", "//:SecondTest", "//:ThirdTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_deletedTest_committed_returnsNoTargets() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest")
    switchToFeatureBranch()
    removeAndCommitTestFileAndResetBuildFile("FirstTest")

    val reportedTargets = runScript()

    // Removing the test should result in no targets being returned (since the test target is gone).
    // Note that if the BUILD file had other tests in it, those would be re-run per the verified
    // behavior of the above test (the BUILD file is considered changed).
    assertThat(reportedTargets).isEmpty()
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_movedTest_staged_returnsNewTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest")
    switchToFeatureBranch()
    moveTest(oldTestName = "FirstTest", newTestName = "RenamedTest", newSubpackage = "newpkg")

    val reportedTargets = runScript()

    // The test should show up under its new name since moving it is the same as changing it.
    assertThat(reportedTargets).containsExactly("//newpkg:RenamedTest")
  }

  @Test
  fun testUtility_featureBranch_multipleTargetsChanged_committed_returnsAffectedTests() {
    initializeEmptyGitRepository()
    createAndCommitBasicTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeAndCommitTestFile("FirstTest")
    changeAndCommitTestFile("ThirdTest")

    val reportedTargets = runScript()

    // Changing multiple tests independently should be reflected in the script's results.
    assertThat(reportedTargets).containsExactly("//:FirstTest", "//:ThirdTest")
  }

  /**
   * Runs the compute_affected_tests utility & returns all of the output lines. Note that the output
   * here is that which is saved directly to the output file, not debug lines printed to the
   * console.
   */
  private fun runScript(): List<String> {
    val outputLog = tempFolder.newFile("output.log")
    main(arrayOf(tempFolder.root.absolutePath, outputLog.absolutePath))
    return outputLog.readLines()
  }

  private fun createEmptyWorkspace() {
    testBazelWorkspace.initEmptyWorkspace()
  }

  private fun initializeEmptyGitRepository() {
    // Initialize the git repository with a base 'develop' branch & an initial empty commit (so that
    // there's a HEAD commit).
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.commit(message = "Initial commit.", allowEmpty = true)
  }

  private fun switchToFeatureBranch() {
    testGitRepository.checkoutNewBranch("introduce-feature")
  }

  /**
   * Creates a new test for each specified test name.
   *
   * @param withGeneratedDependencies whether each test should have a corresponding test dependency
   *     generated
   * @param withExtraDependency if present, an extra library dependency that should be added to each
   *     test
   * @param subpackage if provided, the subpackage under which the tests should be created
   */
  private fun createBasicTests(
    vararg testNames: String,
    withGeneratedDependencies: Boolean = false,
    withExtraDependency: String? = null,
    subpackage: String? = null
  ): List<File> {
    return testNames.flatMap { testName ->
      testBazelWorkspace.createTest(
        testName,
        withGeneratedDependencies,
        withExtraDependency,
        subpackage
      )
    }
  }

  private fun createAndCommitBasicTests(
    vararg testNames: String,
    withGeneratedDependencies: Boolean = false,
    withExtraDependency: String? = null
  ) {
    val changedFiles = createBasicTests(
      *testNames,
      withGeneratedDependencies = withGeneratedDependencies,
      withExtraDependency = withExtraDependency
    )
    testGitRepository.stageFilesForCommit(changedFiles.toSet())
    testGitRepository.commit(message = "Introduce basic tests.")
  }

  private fun changeTestFile(testName: String): File {
    val testFile = testBazelWorkspace.retrieveTestFile(testName)
    testFile.appendText(";") // Add a character to change the file.
    return testFile
  }

  private fun changeDependencyFileForTest(testName: String): File {
    val depFile = testBazelWorkspace.retrieveTestDependencyFile(testName)
    depFile.appendText(";") // Add a character to change the file.
    return depFile
  }

  private fun changeAndStageTestFile(testName: String) {
    val testFile = changeTestFile(testName)
    testGitRepository.stageFileForCommit(testFile)
  }

  private fun changeAndStageDependencyFileForTest(testName: String) {
    val depFile = changeDependencyFileForTest(testName)
    testGitRepository.stageFileForCommit(depFile)
  }

  private fun changeAndCommitTestFile(testName: String) {
    changeAndStageTestFile(testName)
    testGitRepository.commit(message = "Modified test $testName")
  }

  private fun changeAndCommitDependencyFileForTest(testName: String) {
    changeAndStageDependencyFileForTest(testName)
    testGitRepository.commit(message = "Modified dependency for test $testName")
  }

  private fun removeAndCommitTestFileAndResetBuildFile(testName: String) {
    val testFile = testBazelWorkspace.retrieveTestFile(testName)
    testGitRepository.removeFileForCommit(testFile)
    // Clear the test's BUILD file.
    testBazelWorkspace.rootBuildFile.writeText("")
    testGitRepository.commit(message = "Remove test $testName")
  }

  private fun moveTest(oldTestName: String, newTestName: String, newSubpackage: String) {
    // Actually changing the BUILD file for a move is tricky, so just regenerate it, instead, and
    // mark the file as moved.
    val oldTestFile = testBazelWorkspace.retrieveTestFile(oldTestName)
    testBazelWorkspace.rootBuildFile.writeText("")
    val newTestFile = File(tempFolder.root, "$newSubpackage/$newTestName.kt")
    testBazelWorkspace.addTestToBuildFile(newTestName, newTestFile, subpackage = newSubpackage)
    testGitRepository.moveFileForCommit(oldTestFile, newTestFile)
  }

  /** Creates a new library with the specified name & returns its generated target name. */
  private fun createAndCommitLibrary(name: String): String {
    val (targetName, files) = testBazelWorkspace.createLibrary(name)
    testGitRepository.stageFilesForCommit(files.toSet())
    testGitRepository.commit(message = "Add shareable library.")
    return targetName
  }

  private fun changeAndCommitLibrary(name: String) {
    val libFile = testBazelWorkspace.retrieveLibraryFile(name)
    libFile.appendText(";") // Add a character to change the file.
    testGitRepository.stageFileForCommit(libFile)
    testGitRepository.commit(message = "Modified library $name")
  }
}
