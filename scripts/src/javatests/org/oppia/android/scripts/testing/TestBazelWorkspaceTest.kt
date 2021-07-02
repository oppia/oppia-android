package org.oppia.android.scripts.testing

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File
import java.lang.AssertionError
import java.lang.IllegalStateException

/**
 * Tests for [TestBazelWorkspace].
 *
 * Note also that this suite isn't really sufficient for ensuring that the generated Bazel files are
 * actually correct or well formatted. The utility depends on tests that utilize Bazel on the
 * filesystem to ensure correctness. This is because otherwise there's a circular logic dependency:
 * Bazel code is needed to verify the test Bazel utility, but the test Bazel utility is needed to
 * arrange the environment for ensuring that the Bazel code is correct. Rather than utilizing even
 * simple queries to ensure correctness, we leverage both specific expectations tested below & other
 * tests to ensure the utility is operating correctly.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TestBazelWorkspaceTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Test
  fun testCreateTestUtility_doesNotImmediatelyCreateAnyFiles() {
    TestBazelWorkspace(tempFolder)

    // Simply creating the utility should not create any files. This ensures later tests are
    // beginning in a sane state.
    assertThat(tempFolder.root.listFiles()).isEmpty()
  }

  @Test
  fun testInitEmptyWorkspace_emptyDirectory_createsEmptyWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    testBazelWorkspace.initEmptyWorkspace()

    // The WORKSPACE file should now exist, but it won't have any content yet.
    val workspaceFile = File(tempFolder.root, "WORKSPACE")
    assertThat(workspaceFile.exists()).isTrue()
    assertThat(workspaceFile.readLines()).isEmpty()
  }

  @Test
  fun testInitEmptyWorkspace_fileCreationFails_throwsAssertionError() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    // Delete the WORKSPACE after initializing it--this is what puts the workspace in a bad place.
    // Theoretically, the new folder creation could also fail & the underlying assertion check would
    // catch this case and fail.
    testBazelWorkspace.initEmptyWorkspace()
    File(tempFolder.root, "WORKSPACE").delete()

    // Verify that when initializing an empty workspace fails, an AssertionError is thrown (which
    // would fail for calling tests).
    assertThrows(AssertionError::class) { testBazelWorkspace.initEmptyWorkspace() }
  }

  @Test
  fun testRootBuildFileProperty_retrieve_createsBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val buildFile = testBazelWorkspace.rootBuildFile

    // Verify that the BUILD file is a top-level file that exists within the root, but is empty.
    assertThat(buildFile.exists()).isTrue()
    assertThat(buildFile.name).isEqualTo("BUILD.bazel")
    assertThat(buildFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(buildFile.toRelativeString(tempFolder.root)).isEqualTo("BUILD.bazel")
    assertThat(buildFile.readLines()).isEmpty()
  }

  @Test
  fun testRootBuildFileProperty_retrieve_afterCreateTest_isChanged() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val originalLength = testBazelWorkspace.rootBuildFile.length()
    testBazelWorkspace.createTest(testName = "ExampleTest")

    val buildFile = testBazelWorkspace.rootBuildFile

    assertThat(buildFile.length()).isNotEqualTo(originalLength)
  }

  @Test
  fun testRootBuildFileProperty_retrieve_afterCreateLibrary_isChanged() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val originalLength = testBazelWorkspace.rootBuildFile.length()
    testBazelWorkspace.createLibrary(dependencyName = "ExampleLib")

    val buildFile = testBazelWorkspace.rootBuildFile

    assertThat(buildFile.length()).isNotEqualTo(originalLength)
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_nonexistentTestFile_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows(IllegalStateException::class) {
      testBazelWorkspace.addTestToBuildFile(
        testName = "FirstTest",
        testFile = File(tempFolder.root, "FirstTest.kt")
      )
    }

    assertThat(exception).hasMessageThat().contains("FirstTest.kt' does not exist")
  }

  @Test
  fun testAddTestToBuildFile_reusedTestName_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest(testName = "FirstTest")

    val exception = assertThrows(IllegalStateException::class) {
      testBazelWorkspace.addTestToBuildFile(
        testName = "FirstTest",
        testFile = tempFolder.newFile("FirstTestOther.kt")
      )
    }

    assertThat(exception).hasMessageThat().contains("Test 'FirstTest' already set up")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_setsUpWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val workspaceContent = tempFolder.getWorkspaceFile().readAsJoinedString()
    assertThat(workspaceContent).contains("kt_register_toolchains()")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_returnsTestBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    assertThat(files.getFileNames()).containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt")
  }

  @Test
  fun testAddTestToBuildFile_secondTest_doesNotChangeWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )
    val workspaceSize = tempFolder.getWorkspaceFile().length()

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "SecondTest",
      testFile = tempFolder.newFile("SecondTest.kt")
    )

    // WORKSPACE not included since it doesn't need to be reinitialized.
    assertThat(files.getFileNames()).containsExactly("BUILD.bazel", "SecondTest.kt")
    assertThat(workspaceSize).isEqualTo(tempFolder.getWorkspaceFile().length())
  }

  @Test
  fun testAddTestToBuildFile_firstTest_initializesBuildFileOnlyForTests() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(0)
  }

  @Test
  fun testAddTestToBuildFile_secondTest_doesNotReinitializeBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    testBazelWorkspace.addTestToBuildFile(
      testName = "SecondTest",
      testFile = tempFolder.newFile("SecondTest.kt")
    )

    // The load line should only exist once in the file.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_appendsBasicTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    // There should be 1 test in the file with empty deps and correct source.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = []")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withGeneratedDep_configuresBuildFileForLibraries() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true
    )

    // The build file should now be initialized for libraries.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withGeneratedDep_appendsLibraryAndTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true
    )

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\":FirstTestDependency_lib\",]")
    // And the generated library.
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTestDependency.kt\"]")
  }

  @Test
  fun testAddTestToBuildFile_firstTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt", "FirstTestDependency.kt")
  }

  @Test
  fun testAddTestToBuildFile_secondTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt")
    )

    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "SecondTest",
      testFile = tempFolder.newFile("SecondTest.kt"),
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("BUILD.bazel", "SecondTest.kt", "SecondTestDependency.kt")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withExtraDep_appendsTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withExtraDependency = "//:ExtraDep"
    )

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\"//:ExtraDep\",]")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withSubpackage_appendsToSubpackageBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    tempFolder.newFolder("subpackage")
    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("subpackage/FirstTest.kt"),
      subpackage = "subpackage"
    )

    // The root build file shouldn't be changed, but there should be new files in the subpackage
    // directory.
    val subpackageDirectory = File(tempFolder.root, "subpackage")
    assertThat(testBazelWorkspace.rootBuildFile.readLines()).isEmpty()
    assertThat(subpackageDirectory.exists()).isTrue()
    assertThat(subpackageDirectory.isDirectory).isTrue()
    assertThat(File(subpackageDirectory, "BUILD.bazel").exists()).isTrue()
    assertThat(File(subpackageDirectory, "FirstTest.kt").exists()).isTrue()
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withSubpackage_returnsNewBuildAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    tempFolder.newFolder("subpackage")
    val files = testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("subpackage/FirstTest.kt"),
      subpackage = "subpackage"
    )

    assertThat(files.getRelativeFileNames(tempFolder.root))
      .containsExactly("WORKSPACE", "subpackage/BUILD.bazel", "subpackage/FirstTest.kt")
  }

  @Test
  fun testAddTestToBuildFile_unusedTestName_withGeneratedAndExtraDeps_includesBothInTestDeps() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addTestToBuildFile(
      testName = "FirstTest",
      testFile = tempFolder.newFile("FirstTest.kt"),
      withGeneratedDependency = true,
      withExtraDependency = "//:ExtraDep"
    )

    // Both dependencies should be included in the test's deps.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("deps = [\":FirstTestDependency_lib\",\"//:ExtraDep\",]")
  }

  @Test
  fun testCreateTest_reusedTestName_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest(testName = "FirstTest")

    val exception = assertThrows(IllegalStateException::class) {
      testBazelWorkspace.createTest(testName = "FirstTest")
    }

    assertThat(exception).hasMessageThat().contains("Test 'FirstTest' already exists")
  }

  @Test
  fun testCreateTest_firstTest_setsUpWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(testName = "FirstTest")

    val workspaceContent = tempFolder.getWorkspaceFile().readAsJoinedString()
    assertThat(workspaceContent).contains("kt_register_toolchains()")
  }

  @Test
  fun testCreateTest_firstTest_returnsTestBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val files = testBazelWorkspace.createTest(testName = "FirstTest")

    assertThat(files.getFileNames()).containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt")
  }

  @Test
  fun testCreateTest_secondTest_doesNotChangeWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest(testName = "FirstTest")
    val workspaceSize = tempFolder.getWorkspaceFile().length()

    val files = testBazelWorkspace.createTest(testName = "SecondTest")

    // WORKSPACE not included since it doesn't need to be reinitialized.
    assertThat(files.getFileNames()).containsExactly("BUILD.bazel", "SecondTest.kt")
    assertThat(workspaceSize).isEqualTo(tempFolder.getWorkspaceFile().length())
  }

  @Test
  fun testCreateTest_firstTest_initializesBuildFileOnlyForTests() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(testName = "FirstTest")

    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(0)
  }

  @Test
  fun testCreateTest_secondTest_doesNotReinitializeBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest(testName = "FirstTest")

    testBazelWorkspace.createTest(testName = "SecondTest")

    // The load line should only exist once in the file.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_test")).isEqualTo(1)
  }

  @Test
  fun testCreateTest_unusedTestName_appendsBasicTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(testName = "FirstTest")

    // There should be 1 test in the file with empty deps and correct source.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = []")
  }

  @Test
  fun testCreateTest_unusedTestName_withGeneratedDep_configuresBuildFileForLibraries() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true
    )

    // The build file should now be initialized for libraries.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
  }

  @Test
  fun testCreateTest_unusedTestName_withGeneratedDep_appendsLibraryAndTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true
    )

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\":FirstTestDependency_lib\",]")
    // And the generated library.
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTestDependency.kt\"]")
  }

  @Test
  fun testCreateTest_firstTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val files = testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("WORKSPACE", "BUILD.bazel", "FirstTest.kt", "FirstTestDependency.kt")
  }

  @Test
  fun testCreateTest_secondTest_withGeneratedDep_returnsTestDepBuildWorkspaceFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest(testName = "FirstTest")

    val files = testBazelWorkspace.createTest(
      testName = "SecondTest",
      withGeneratedDependency = true
    )

    assertThat(files.getFileNames())
      .containsExactly("BUILD.bazel", "SecondTest.kt", "SecondTestDependency.kt")
  }

  @Test
  fun testCreateTest_unusedTestName_withExtraDep_appendsTestWithDep() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(testName = "FirstTest", withExtraDependency = "//:ExtraDep")

    // Ensure the test is arranged correctly.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("srcs = [\"FirstTest.kt\"]")
    assertThat(buildContent).contains("deps = [\"//:ExtraDep\",]")
  }

  @Test
  fun testCreateTest_unusedTestName_withSubpackage_appendsToSubpackageBuildFile() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(testName = "FirstTest", subpackage = "subpackage")

    // The root build file shouldn't be changed, but there should be new files in the subpackage
    // directory.
    val subpackageDirectory = File(tempFolder.root, "subpackage")
    assertThat(testBazelWorkspace.rootBuildFile.readLines()).isEmpty()
    assertThat(subpackageDirectory.exists()).isTrue()
    assertThat(subpackageDirectory.isDirectory).isTrue()
    assertThat(File(subpackageDirectory, "BUILD.bazel").exists()).isTrue()
    assertThat(File(subpackageDirectory, "FirstTest.kt").exists()).isTrue()
  }

  @Test
  fun testCreateTest_unusedTestName_withSubpackage_returnsNewBuildAndTestFiles() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val files = testBazelWorkspace.createTest(testName = "FirstTest", subpackage = "subpackage")

    assertThat(files.getRelativeFileNames(tempFolder.root))
      .containsExactly("WORKSPACE", "subpackage/BUILD.bazel", "subpackage/FirstTest.kt")
  }

  @Test
  fun testCreateTest_unusedTestName_withGeneratedAndExtraDeps_includesBothInTestDeps() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createTest(
      testName = "FirstTest",
      withGeneratedDependency = true,
      withExtraDependency = "//:ExtraDep"
    )

    // Both dependencies should be included in the test's deps.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_test\\(")).isEqualTo(1)
    assertThat(buildContent).contains("deps = [\":FirstTestDependency_lib\",\"//:ExtraDep\",]")
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_configuresWorkspaceAndBuild() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    val workspaceContent = tempFolder.getWorkspaceFile().readAsJoinedString()
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(workspaceContent).contains("kt_register_toolchains()")
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_appendsJvmLibraryDeclaration() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(1)
    assertThat(buildContent).contains("name = \"ExampleDep_lib\"")
    assertThat(buildContent).contains("srcs = [\"ExampleDep.kt\"]")
  }

  @Test
  fun testCreateLibrary_firstLib_unusedName_returnsBuildLibAndWorkspaceFilesWithTargetName() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()

    val (targetName, files) = testBazelWorkspace.createLibrary(dependencyName = "ExampleDep")

    assertThat(targetName).isEqualTo("ExampleDep_lib")
    assertThat(files.getFileNames()).containsExactly("WORKSPACE", "BUILD.bazel", "ExampleDep.kt")
  }

  @Test
  fun testCreateLibrary_secondLib_unusedName_doesNotChangeWorkspace() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")
    val workspaceSize = tempFolder.getWorkspaceFile().length()

    val (_, files) = testBazelWorkspace.createLibrary(dependencyName = "SecondLib")

    assertThat(files.getFileNames()).doesNotContain("WORKSPACE")
    assertThat(workspaceSize).isEqualTo(tempFolder.getWorkspaceFile().length())
  }

  @Test
  fun testCreateLibrary_secondLib_unusedName_appendsJvmLibraryDeclaration() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    testBazelWorkspace.createLibrary(dependencyName = "SecondLib")

    // The kt_jvm_library declaration should only exist once, and both libraries should exist.
    val buildContent = testBazelWorkspace.rootBuildFile.readAsJoinedString()
    assertThat(buildContent.countMatches("load\\(.+?kt_jvm_library")).isEqualTo(1)
    assertThat(buildContent.countMatches("kt_jvm_library\\(")).isEqualTo(2)
    assertThat(buildContent).contains("name = \"FirstLib_lib\"")
    assertThat(buildContent).contains("srcs = [\"FirstLib.kt\"]")
    assertThat(buildContent).contains("name = \"SecondLib_lib\"")
    assertThat(buildContent).contains("srcs = [\"SecondLib.kt\"]")
  }

  @Test
  fun testCreateLibrary_secondLib_reusedName_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    val exception = assertThrows(IllegalStateException::class) {
      testBazelWorkspace.createLibrary(dependencyName = "FirstLib")
    }

    assertThat(exception).hasMessageThat().contains("Library 'FirstLib' already exists")
  }

  @Test
  fun testWorkspaceInitialization_createLibrary_thenTest_workspaceInitedForKotlinForLibrary() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    testBazelWorkspace.createTest(testName = "FirstTest")

    // The workspace should only be configured once (due to the library initialization).
    val workspaceContent = tempFolder.getWorkspaceFile().readAsJoinedString()
    assertThat(workspaceContent.countMatches("http_archive\\(")).isEqualTo(1)
  }

  @Test
  fun testWorkspaceInitialization_createTest_thenLibrary_workspaceInitedForKotlinForTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "FirstTest")

    testBazelWorkspace.createLibrary(dependencyName = "FirstLib")

    // The workspace should only be configured once (due to the test initialization).
    val workspaceContent = tempFolder.getWorkspaceFile().readAsJoinedString()
    assertThat(workspaceContent.countMatches("http_archive\\(")).isEqualTo(1)
  }

  @Test
  fun testRetrieveTestFile_noTest_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    // A non-existent test file cannot be retrieved.
    assertThrows(NoSuchElementException::class) {
      testBazelWorkspace.retrieveTestFile(testName = "Invalid")
    }
  }

  @Test
  fun testRetrieveTestFile_forRealTest_returnsFileForTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest(testName = "ExampleTest")

    val testFile = testBazelWorkspace.retrieveTestFile(testName = "ExampleTest")

    assertThat(testFile.exists()).isTrue()
    assertThat(testFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(testFile.name).isEqualTo("ExampleTest.kt")
  }

  @Test
  fun testRetrieveLibraryFile_noLibrary_throwsException() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    // A non-existent library file cannot be retrieved.
    assertThrows(NoSuchElementException::class) {
      testBazelWorkspace.retrieveLibraryFile(dependencyName = "Invalid")
    }
  }

  @Test
  fun testRetrieveLibraryFile_forRealLibrary_returnsFileForLibrary() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createLibrary(dependencyName = "ExampleLib")

    val libFile = testBazelWorkspace.retrieveLibraryFile(dependencyName = "ExampleLib")

    assertThat(libFile.exists()).isTrue()
    assertThat(libFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(libFile.name).isEqualTo("ExampleLib.kt")
  }

  @Test
  fun testRetrieveTestDependencyFile_noTest_throwsExceptionWithHelpfulMessage() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)

    val exception = assertThrows(IllegalStateException::class) {
      testBazelWorkspace.retrieveTestDependencyFile(testName = "Invalid")
    }

    assertThat(exception).hasMessageThat().contains("No entry for 'Invalid'.")
    assertThat(exception).hasMessageThat().contains("Was the test created without dependencies?")
  }

  @Test
  fun testRetrieveTestDependencyFile_testWithoutGeneratedDep_throwsExceptionWithHelpfulMessage() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest("ValidWithoutDep")

    val exception = assertThrows(IllegalStateException::class) {
      testBazelWorkspace.retrieveTestDependencyFile(testName = "ValidWithoutDep")
    }

    // Since the test does not have a generated dependency, there is no entry to retrieve.
    assertThat(exception).hasMessageThat().contains("No entry for 'ValidWithoutDep'.")
    assertThat(exception).hasMessageThat().contains("Was the test created without dependencies?")
  }

  @Test
  fun testRetrieveTestDependencyFile_testWithGeneratedDep_returnsFileForTest() {
    val testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testBazelWorkspace.createTest("ValidWithDep", withGeneratedDependency = true)

    val libFile = testBazelWorkspace.retrieveTestDependencyFile(testName = "ValidWithDep")

    assertThat(libFile.exists()).isTrue()
    assertThat(libFile.isRelativeTo(tempFolder.root)).isTrue()
    assertThat(libFile.name).isEqualTo("ValidWithDepDependency.kt")
  }

  private fun TemporaryFolder.getWorkspaceFile(): File = File(root, "WORKSPACE")

  private fun File.readAsJoinedString(): String = readLines().joinToString(separator = "\n")

  private fun File.isRelativeTo(base: File): Boolean = relativeToOrNull(base) != null

  private fun Iterable<File>.getFileNames(): List<String> = map { it.name }

  private fun Iterable<File>.getRelativeFileNames(root: File): List<String> = map {
    it.toRelativeString(root)
  }

  private fun String.countMatches(regex: String): Int = Regex(regex).findAll(this).toList().size
}
