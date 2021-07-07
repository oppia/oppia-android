package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.mockito.anyOrNull

/**
 * Tests for [BazelClient].
 *
 * Note that this test executes real commands on the local filesystem & requires Bazel in the local
 * environment.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BazelClientTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  @Mock lateinit var mockCommandExecutor: CommandExecutor

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
  }

  @Test
  fun testRetrieveTestTargets_emptyFolder_fails() {
    val bazelClient = BazelClient(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) {
      bazelClient.retrieveAllTestTargets()
    }

    // Verify that the underlying Bazel command failed since it was run outside a Bazel workspace.
    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("only supported from within a workspace")
  }

  @Test
  fun testRetrieveTestTargets_emptyWorkspace_fails() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows(IllegalStateException::class) {
      bazelClient.retrieveAllTestTargets()
    }

    // Verify that the underlying Bazel command failed since there are no test targets.
    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no targets found beneath ''")
  }

  @Test
  fun testRetrieveTestTargets_workspaceWithTest_returnsTestTarget() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("ExampleTest")

    val testTargets = bazelClient.retrieveAllTestTargets()

    assertThat(testTargets).contains("//:ExampleTest")
  }

  @Test
  fun testRetrieveTestTargets_workspaceWithMultipleTests_returnsTestTargets() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest")

    val testTargets = bazelClient.retrieveAllTestTargets()

    assertThat(testTargets).containsExactly("//:FirstTest", "//:SecondTest")
  }

  @Test
  fun testRetrieveTestTargets_resultsJumbled_returnsCorrectTestTargets() {
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutor)
    fakeCommandExecutorWithResult(singleLine = "//:FirstTest//:SecondTest")

    val testTargets = bazelClient.retrieveAllTestTargets()

    assertThat(testTargets).containsExactly("//:FirstTest", "//:SecondTest")
  }

  @Test
  fun testRetrieveBazelTargets_forFileNotInBuildGraph_returnsEmptyList() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    tempFolder.newFile("filenotingraph")

    val fileTargets = bazelClient.retrieveBazelTargets(listOf("filenotingraph"))

    assertThat(fileTargets).isEmpty()
  }

  @Test
  fun testRetrieveBazelTargets_forTestFile_returnsBazelTarget() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")

    val fileTargets = bazelClient.retrieveBazelTargets(listOf("FirstTest.kt"))

    assertThat(fileTargets).containsExactly("//:FirstTest.kt")
  }

  @Test
  fun testRetrieveBazelTargets_forMultipleMixedFiles_returnsBazelTargets() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest", withGeneratedDependency = true)
    testBazelWorkspace.createTest("ThirdTest", subpackage = "subpackage")
    testBazelWorkspace.createLibrary("ExtraDep")

    val fileTargets =
      bazelClient.retrieveBazelTargets(
        listOf("SecondTestDependency.kt", "subpackage/ThirdTest.kt", "ExtraDep.kt")
      )

    assertThat(fileTargets).containsExactly(
      "//:SecondTestDependency.kt", "//subpackage:ThirdTest.kt", "//:ExtraDep.kt"
    )
  }

  @Test
  fun testRetrieveBazelTargets_resultsJumbled_returnsCorrectBazelTargets() {
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutor)
    fakeCommandExecutorWithResult(singleLine = "//:FirstTest.kt//:SecondTest.kt")

    val fileTargets = bazelClient.retrieveBazelTargets(listOf("FirstTest.kt", "SecondTest.kt"))

    assertThat(fileTargets).containsExactly("//:FirstTest.kt", "//:SecondTest.kt")
  }

  @Test
  fun testRetrieveRelatedTestTargets_forTargetWithNoTestDependency_returnsNoTargets() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createLibrary("SomeDependency")
    testBazelWorkspace.createTest("FirstTest")

    val testTargets = bazelClient.retrieveRelatedTestTargets(listOf("//:SomeDependency.kt"))

    // Since the target doesn't have any tests depending on it, there are no targets to provide.
    assertThat(testTargets).isEmpty()
  }

  @Test
  fun testRetrieveRelatedTestTargets_forTestFileTarget_returnsTestTarget() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")

    val testTargets = bazelClient.retrieveRelatedTestTargets(listOf("//:FirstTest.kt"))

    assertThat(testTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testRetrieveRelatedTestTargets_forDependentFileTarget_returnsTestTarget() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest", withGeneratedDependency = true)

    val testTargets = bazelClient.retrieveRelatedTestTargets(listOf("//:FirstTestDependency.kt"))

    assertThat(testTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testRetrieveRelatedTestTargets_forMixedFileTargets_returnsRelatedTestTargets() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createLibrary("ExtraDep")
    testBazelWorkspace.createTest("FirstTest", withExtraDependency = "//:ExtraDep_lib")
    testBazelWorkspace.createTest("SecondTest", withGeneratedDependency = true)
    testBazelWorkspace.createTest("ThirdTest")
    testBazelWorkspace.createTest("FourthTest", subpackage = "subpackage")

    val testTargets =
      bazelClient.retrieveRelatedTestTargets(
        listOf("//:SecondTestDependency.kt", "//subpackage:FourthTest.kt", "//:ExtraDep.kt")
      )

    println(testBazelWorkspace.rootBuildFile.readLines().joinToString("\n"))

    // The function should provide all test targets related to the file targets provided (either via
    // dependencies or because that file is part of the test itself).
    assertThat(testTargets).containsExactly(
      "//:FirstTest", "//:SecondTest", "//subpackage:FourthTest"
    )
  }

  @Test
  fun testRetrieveRelatedTestTargets_resultsJumbled_returnsCorrectTestTargets() {
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutor)
    fakeCommandExecutorWithResult(singleLine = "//:FirstTest//:SecondTest")

    val testTargets =
      bazelClient.retrieveRelatedTestTargets(listOf("//:FirstTest.kt", "//:SecondTest.kt"))

    assertThat(testTargets).containsExactly("//:FirstTest", "//:SecondTest")
  }

  @Test
  fun testRetrieveTransitiveTestTargets_forNoFiles_returnsEmptyList() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf())

    // No test targets for no related build files.
    assertThat(testTargets).isEmpty()
  }

  @Test
  fun testRetrieveTransitiveTestTargets_forBuildFile_returnsAllTestsInThatBuildFile() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest")
    testBazelWorkspace.createTest("ThirdTest", subpackage = "subpackage")
    testBazelWorkspace.createTest("FourthTest")

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf("BUILD.bazel"))

    // No test targets for no related build files.
    assertThat(testTargets).containsExactly("//:FirstTest", "//:SecondTest", "//:FourthTest")
  }

  @Test
  fun testRetrieveTransitiveTestTargets_forMultipleBuildFiles_returnsAllRelatedTests() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest", subpackage = "two")
    testBazelWorkspace.createTest("ThirdTest", subpackage = "three")
    testBazelWorkspace.createTest("FourthTest", subpackage = "four")

    val testTargets =
      bazelClient.retrieveTransitiveTestTargets(listOf("two/BUILD.bazel", "three/BUILD.bazel"))

    // No test targets for no related build files.
    assertThat(testTargets).containsExactly("//two:SecondTest", "//three:ThirdTest")
  }

  @Test
  @Ignore("Fails in GitHub Actions") // TODO(#2691): Re-enable this test once it can pass in CI.
  fun testRetrieveTransitiveTestTargets_forWorkspace_returnsAllTests() {
    val bazelClient = BazelClient(tempFolder.root)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest", subpackage = "two")
    testBazelWorkspace.createTest("ThirdTest", subpackage = "three")
    testBazelWorkspace.createTest("FourthTest")

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf("WORKSPACE"))

    // No test targets for no related build files.
    assertThat(testTargets).containsExactly(
      "//:FirstTest", "//two:SecondTest", "//three:ThirdTest", "//:FourthTest"
    )
  }

  @Test
  fun testRetrieveTransitiveTestTargets_resultsJumbled_returnsCorrectTestTargets() {
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutor)
    fakeCommandExecutorWithResult(singleLine = "//:FirstTest//:SecondTest")

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf("WORKSPACE"))

    assertThat(testTargets).containsExactly("//:FirstTest", "//:SecondTest")
  }

  private fun fakeCommandExecutorWithResult(singleLine: String) {
    // Fake a Bazel command's results to return jumbled results. This has been observed to happen
    // sometimes in CI, but doesn't have a known cause. The utility is meant to de-jumble these in
    // circumstances where they occur, and the only way to guarantee this happens in the test
    // environment is to force the command output.
    `when`(mockCommandExecutor.executeCommand(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
      .thenReturn(
        CommandResult(
          exitCode = 0,
          output = listOf(singleLine),
          errorOutput = listOf(),
          command = listOf()
        )
      )
  }
}
