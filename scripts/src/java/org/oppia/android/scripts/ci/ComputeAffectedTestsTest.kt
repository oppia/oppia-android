package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

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
  private lateinit var testGitWorkspace: TestGitWorkspace

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testGitWorkspace = TestGitWorkspace(tempFolder)
  }

  @After
  fun tearDown() {
    // Print the status of the git repository to help with debugging in the cases of test failures
    // and to help manually verify the expect git state at the end of each test.
    println("git status (at end of test):")
    println(testGitWorkspace.status())
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
    return outputLog.bufferedReader().readLines()
  }

  private fun createEmptyWorkspace() {
    testBazelWorkspace.initEmptyWorkspace()
  }

  private fun initializeEmptyGitRepository() {
    // Initialize the git repository with a base 'develop' branch & an initial empty commit (so that
    // there's a HEAD commit).
    testGitWorkspace.init()
    testGitWorkspace.setUser(email = "test@oppia.org", name = "Test User")
    testGitWorkspace.checkoutNewBranch("develop")
    testGitWorkspace.commit(message = "Initial commit.", allowEmpty = true)
  }

  private fun switchToFeatureBranch() {
    testGitWorkspace.checkoutNewBranch("introduce-feature")
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
    testGitWorkspace.stageFilesForCommit(changedFiles.toSet())
    testGitWorkspace.commit(message = "Introduce basic tests.")
  }

  private fun changeTestFile(testName: String): File {
    val testFile = testBazelWorkspace.retrieveTestFile(testName)
    testFile.appendingPrintWriter().use { writer ->
      writer.println(";") // Add a character to change the file.
    }
    return testFile
  }

  private fun changeDependencyFileForTest(testName: String): File {
    val depFile = testBazelWorkspace.retrieveTestDependencyFile(testName)
    depFile.appendingPrintWriter().use { writer ->
      writer.println(";") // Add a character to change the file.
    }
    return depFile
  }

  private fun changeAndStageTestFile(testName: String) {
    val testFile = changeTestFile(testName)
    testGitWorkspace.stageFileForCommit(testFile)
  }

  private fun changeAndStageDependencyFileForTest(testName: String) {
    val depFile = changeDependencyFileForTest(testName)
    testGitWorkspace.stageFileForCommit(depFile)
  }

  private fun changeAndCommitTestFile(testName: String) {
    changeAndStageTestFile(testName)
    testGitWorkspace.commit(message = "Modified test $testName")
  }

  private fun changeAndCommitDependencyFileForTest(testName: String) {
    changeAndStageDependencyFileForTest(testName)
    testGitWorkspace.commit(message = "Modified dependency for test $testName")
  }

  private fun removeAndCommitTestFileAndResetBuildFile(testName: String) {
    val testFile = testBazelWorkspace.retrieveTestFile(testName)
    testGitWorkspace.removeFileForCommit(testFile)
    // Clear the test's BUILD file.
    testBazelWorkspace.rootBuildFile.writeText("")
    testGitWorkspace.commit(message = "Remove test $testName")
  }

  private fun moveTest(oldTestName: String, newTestName: String, newSubpackage: String) {
    // Actually changing the BUILD file for a move is tricky, so just regenerate it, instead, and
    // mark the file as moved.
    val oldTestFile = testBazelWorkspace.retrieveTestFile(oldTestName)
    testBazelWorkspace.rootBuildFile.writeText("")
    val newTestFile = File(tempFolder.root, "$newSubpackage/$newTestName.kt")
    testBazelWorkspace.addTestToBuildFile(newTestName, newTestFile, subpackage = newSubpackage)
    testGitWorkspace.moveFileForCommit(oldTestFile, newTestFile)
  }

  /** Creates a new library with the specified name & returns its generated target name. */
  private fun createAndCommitLibrary(name: String): String {
    val (targetName, files) = testBazelWorkspace.createLibrary(name)
    testGitWorkspace.stageFilesForCommit(files.toSet())
    testGitWorkspace.commit(message = "Add shareable library.")
    return targetName
  }

  private fun changeAndCommitLibrary(name: String) {
    val libFile = testBazelWorkspace.retrieveLibraryFile(name)
    libFile.appendingPrintWriter().use { writer ->
      writer.println(";") // Add a character to change the file.
    }
    testGitWorkspace.stageFileForCommit(libFile)
    testGitWorkspace.commit(message = "Modified library $name")
  }

  // TODO: extract to top-level file
  private class TestBazelWorkspace(private val temporaryRootFolder: TemporaryFolder) {
    private val workspaceFile by lazy { temporaryRootFolder.newFile("WORKSPACE") }
    val rootBuildFile: File by lazy { temporaryRootFolder.newFile("BUILD.bazel") }
    private val testFileMap = mutableMapOf<String, File>()
    private val libraryFileMap = mutableMapOf<String, File>()
    private val testDependencyNameMap = mutableMapOf<String, String>()
    private var isConfiguredForKotlin = false
    private var isConfiguredForTests = false
    private var isConfiguredForLibraries = false

    fun initEmptyWorkspace() {
      // Sanity check, but in reality this is just initializing workspaceFile to ensure that it
      // exists.
      assertThat(workspaceFile.exists()).isTrue()
    }

    fun addTestToBuildFile(
      testName: String,
      testFile: File,
      withGeneratedDependency: Boolean = false,
      withExtraDependency: String? = null,
      subpackage: String? = null
    ): List<File> {
      val prereqFiles = ensureWorkspaceIsConfiguredForTests()
      val (dependencyTargetName, libPrereqFiles) = if (withGeneratedDependency) {
        createLibrary("${testName}Dependency")
      } else null to listOf()
      val buildFile = if (subpackage != null) {
        if (!File(temporaryRootFolder.root, subpackage).exists()) {
          temporaryRootFolder.newFolder(subpackage)
        }
        val newBuildFile = temporaryRootFolder.newFile("$subpackage/BUILD.bazel")
        prepareBuildFileForTests(newBuildFile)
        newBuildFile
      } else rootBuildFile
      return buildFile.appendingPrintWriter().use { writer ->
        testFileMap[testName] = testFile
        writer.println("kt_jvm_test(")
        writer.println("    name = \"$testName\",")
        writer.println("    srcs = [\"${testFile.name}\"],")
        writer.println("    deps = [")
        if (withGeneratedDependency) {
          testDependencyNameMap[testName] = dependencyTargetName ?: error("Something went wrong.")
          writer.print("        \":$dependencyTargetName\",")
        }
        withExtraDependency?.let { writer.println("        \"$it\",") }
        writer.println("    ],")
        writer.println(")")
        return@use listOf(testFile, buildFile) + prereqFiles + libPrereqFiles
      }
    }

    fun createTest(
      testName: String,
      withGeneratedDependency: Boolean = false,
      withExtraDependency: String? = null,
      subpackage: String? = null
    ): List<File> {
      val testFile = if (subpackage != null) {
        if (!File(temporaryRootFolder.root, subpackage).exists()) {
          temporaryRootFolder.newFolder(subpackage)
        }
        temporaryRootFolder.newFile("$subpackage/$testName.kt")
      } else temporaryRootFolder.newFile("$testName.kt")
      return addTestToBuildFile(
        testName,
        testFile,
        withGeneratedDependency,
        withExtraDependency,
        subpackage
      )
    }

    fun createLibrary(dependencyName: String): Pair<String, List<File>> {
      val prereqFiles = ensureWorkspaceIsConfiguredForLibraries()
      return rootBuildFile.appendingPrintWriter().use { writer ->
        val depFile = temporaryRootFolder.newFile("$dependencyName.kt")
        val libTargetName = "${dependencyName}_lib"
        libraryFileMap[libTargetName] = depFile
        writer.println("kt_jvm_library(")
        writer.println("    name = \"$libTargetName\",")
        writer.println("    srcs = [\"${depFile.name}\"],")
        writer.println(")")
        return@use libTargetName to listOf(depFile, rootBuildFile) + prereqFiles
      }
    }

    fun retrieveTestFile(testName: String): File = testFileMap.getValue(testName)

    fun retrieveLibraryFile(libraryName: String): File =
      libraryFileMap.getValue("${libraryName}_lib")

    fun retrieveTestDependencyFile(testName: String): File {
      return libraryFileMap.getValue(
        testDependencyNameMap[testName]
          ?: error("No entry for $testName. Was the test created without dependencies?")
      )
    }

    private fun ensureWorkspaceIsConfiguredForKotlin(): List<File> {
      if (!isConfiguredForKotlin) {
        workspaceFile.appendingPrintWriter().use { writer ->
          // Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
          writer.println("load(\"@bazel_tools//tools/build_defs/repo:http.bzl\", \"http_archive\")")
          writer.println("http_archive(")
          writer.println("    name = \"io_bazel_rules_kotlin\",")
          writer.println(
            "    sha256 = \"6194a864280e1989b6d8118a4aee03bb50edeeae4076e5bc30eef8a9" +
              "8dcd4f07\","
          )
          writer.println(
            "    urls = [\"https://github.com/bazelbuild/rules_kotlin/releases" +
              "/download/v1.5.0-alpha-2/rules_kotlin_release.tgz\"],"
          )
          writer.println(")")
          writer.println(
            "load(\"@io_bazel_rules_kotlin//kotlin:dependencies.bzl\"," +
              " \"kt_download_local_dev_dependencies\")"
          )
          writer.println(
            "load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\"," +
              " \"kotlin_repositories\", \"kt_register_toolchains\")"
          )
          writer.println("kt_download_local_dev_dependencies()")
          writer.println("kotlin_repositories()")
          writer.println("kt_register_toolchains()")
        }
        isConfiguredForKotlin = true
        return listOf(workspaceFile)
      }
      return listOf()
    }

    private fun ensureWorkspaceIsConfiguredForTests(): List<File> {
      val affectedFiles = if (!isConfiguredForTests) {
        prepareBuildFileForTests(rootBuildFile)
        isConfiguredForTests = true
        listOf(rootBuildFile)
      } else listOf()
      return ensureWorkspaceIsConfiguredForKotlin() + affectedFiles
    }

    private fun ensureWorkspaceIsConfiguredForLibraries(): List<File> {
      val affectedFiles = if (!isConfiguredForLibraries) {
        rootBuildFile.appendingPrintWriter().use { writer ->
          writer.println("load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\", \"kt_jvm_library\")")
        }
        isConfiguredForLibraries = true
        listOf(rootBuildFile)
      } else listOf()
      return ensureWorkspaceIsConfiguredForKotlin() + affectedFiles
    }

    private fun prepareBuildFileForTests(buildFile: File) {
      buildFile.appendingPrintWriter().use { writer ->
        writer.println("load(\"@io_bazel_rules_kotlin//kotlin:kotlin.bzl\", \"kt_jvm_test\")")
      }
    }
  }

  // TODO: extract to top-level file
  private class TestGitWorkspace(private val temporaryRootFolder: TemporaryFolder) {
    private val rootDirectory by lazy { temporaryRootFolder.root }

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
      return executeCommand(rootDirectory, "git", "status").output.joinOutputString()
    }

    private fun executeSuccessfulGitCommand(vararg arguments: String) {
      verifySuccessfulCommand(executeCommand(rootDirectory, "git", *arguments))
    }

    private fun verifySuccessfulCommand(result: CommandResult) {
      assertWithMessage("Output: ${result.output.joinOutputString()}")
        .that(result.exitCode)
        .isEqualTo(0)
    }

    private fun List<String>.joinOutputString(): String = joinToString(separator = "\n") { "  $it" }
  }
}

// A version of File.printWriter() that appends content rather than overwriting it.
private fun File.appendingPrintWriter(): PrintWriter =
  PrintWriter(FileOutputStream(this, /* append= */ true).bufferedWriter())
