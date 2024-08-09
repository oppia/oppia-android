package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
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
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Tests for [BazelClient].
 *
 * Note that this test executes real commands on the local filesystem & requires Bazel in the local
 * environment.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test
// Function name: test names are conventionally named with underscores
@Suppress("SameParameterValue", "FunctionName")
class BazelClientTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }
  private lateinit var testBazelWorkspace: TestBazelWorkspace

  @Mock lateinit var mockCommandExecutor: CommandExecutor

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Before
  fun setUp() {
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
  }

  @Test
  fun testRetrieveTestTargets_emptyFolder_fails() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      bazelClient.retrieveAllTestTargets()
    }

    // Verify that the underlying Bazel command failed since it was run outside a Bazel workspace.
    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("only supported from within a workspace")
  }

  @Test
  fun testRetrieveTestTargets_emptyWorkspace_fails() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows<IllegalStateException>() {
      bazelClient.retrieveAllTestTargets()
    }

    // Verify that the underlying Bazel command failed since there are no test targets.
    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no targets found beneath ''")
  }

  @Test
  fun testRetrieveTestTargets_workspaceWithTest_returnsTestTarget() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("ExampleTest")

    val testTargets = bazelClient.retrieveAllTestTargets()

    assertThat(testTargets).contains("//:ExampleTest")
  }

  @Test
  fun testRetrieveTestTargets_workspaceWithMultipleTests_returnsTestTargets() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
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
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    tempFolder.newFile("filenotingraph")

    val fileTargets = bazelClient.retrieveBazelTargets(listOf("filenotingraph"))

    assertThat(fileTargets).isEmpty()
  }

  @Test
  fun testRetrieveBazelTargets_forTestFile_returnsBazelTarget() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")

    val fileTargets = bazelClient.retrieveBazelTargets(listOf("FirstTest.kt"))

    assertThat(fileTargets).containsExactly("//:FirstTest.kt")
  }

  @Test
  fun testRetrieveBazelTargets_forMultipleMixedFiles_returnsBazelTargets() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
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
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createLibrary("SomeDependency")
    testBazelWorkspace.createTest("FirstTest")

    val testTargets = bazelClient.retrieveRelatedTestTargets(listOf("//:SomeDependency.kt"))

    // Since the target doesn't have any tests depending on it, there are no targets to provide.
    assertThat(testTargets).isEmpty()
  }

  @Test
  fun testRetrieveRelatedTestTargets_forTestFileTarget_returnsTestTarget() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")

    val testTargets = bazelClient.retrieveRelatedTestTargets(listOf("//:FirstTest.kt"))

    assertThat(testTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testRetrieveRelatedTestTargets_forDependentFileTarget_returnsTestTarget() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest", withGeneratedDependency = true)

    val testTargets = bazelClient.retrieveRelatedTestTargets(listOf("//:FirstTestDependency.kt"))

    assertThat(testTargets).containsExactly("//:FirstTest")
  }

  @Test
  fun testRetrieveRelatedTestTargets_forMixedFileTargets_returnsRelatedTestTargets() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
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
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf())

    // No test targets for no related build files.
    assertThat(testTargets).isEmpty()
  }

  @Test
  fun testRetrieveTransitiveTestTargets_forBuildFile_returnsAllTestsInThatBuildFile() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
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
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
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
  fun testRetrieveTransitiveTestTargets_forBzlFile_returnsRelatedTests() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    // Generate tests.
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest", subpackage = "two")
    testBazelWorkspace.createTest("ThirdTest", subpackage = "three")
    testBazelWorkspace.createTest("FourthTest")
    // Generate Bazel file that will be added into the build graph.
    val bzlFile =
      generateCustomJvmTestRuleBazelFile(
        "custom_jvm_test_rule_base.bzl",
        "custom_jvm_test_rule.bzl"
      )
    // Update build files to depend on the new Bazel file.
    val packageTwoDirectory = File(tempFolder.root, "two")
    updateBuildFileToUseCustomJvmTestRule(bzlFile, File(tempFolder.root, "BUILD.bazel"))
    updateBuildFileToUseCustomJvmTestRule(bzlFile, File(packageTwoDirectory, "BUILD.bazel"))

    val testTargets =
      bazelClient.retrieveTransitiveTestTargets(
        listOf("custom_jvm_test_rule_base.bzl", "custom_jvm_test_rule.bzl")
      )

    // All tests corresponding to build files that use the affected .bzl file should be returned.
    assertThat(testTargets).containsExactly("//:FirstTest", "//two:SecondTest", "//:FourthTest")
  }

  @Test
  fun testRetrieveTransitiveTestTargets_forWorkspace_returnsAllTests() {
    val bazelClient = BazelClient(tempFolder.root, commandExecutor)
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.createTest("FirstTest")
    testBazelWorkspace.createTest("SecondTest", subpackage = "two")
    testBazelWorkspace.createTest("ThirdTest", subpackage = "three")
    testBazelWorkspace.createTest("FourthTest")

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf("WORKSPACE"))

    // All test targets should be returned for WORKSPACE since it affects all files.
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

  @Test
  fun testRetrieveMavenDepsList_binaryDependsOnArtifactViaThirdParty_returnsArtifact() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("androidx.annotation:annotation:1.1.0")
    )
    tempFolder.newFile("AndroidManifest.xml")
    createAndroidBinary(
      binaryName = "test_oppia",
      manifestName = "AndroidManifest.xml",
      dependencyName = "//third_party:androidx_annotation_annotation",
    )
    tempFolder.newFolder("third_party")
    val thirdPartyBuild = tempFolder.newFile("third_party/BUILD.bazel")
    createAndroidLibrary(
      artifactName = "androidx.annotation:annotation:1.1.0",
      buildFile = thirdPartyBuild
    )
    val bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    val thirdPartyDependenciesList =
      bazelClient.retrieveThirdPartyMavenDepsListForBinary("//:test_oppia")

    assertThat(thirdPartyDependenciesList).contains("@maven//:androidx_annotation_annotation")
  }

  @Test
  fun testRetrieveMavenDepsList_binaryDependsOnArtifactNotViaThirdParty_doesNotReturnArtifact() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("androidx.annotation:annotation:1.1.0")
    )
    tempFolder.newFile("AndroidManifest.xml")
    createAndroidBinary(
      binaryName = "test_oppia",
      manifestName = "AndroidManifest.xml",
      dependencyName = ":androidx_annotation_annotation"
    )
    tempFolder.newFolder("third_party")
    val thirdPartyBuild = tempFolder.newFile("third_party/BUILD.bazel")
    createAndroidLibrary(
      artifactName = "io.fabric.sdk.android:fabric:1.4.7",
      buildFile = thirdPartyBuild
    )
    createAndroidLibrary(
      artifactName = "androidx.annotation:annotation:1.1.0",
      buildFile = testBazelWorkspace.rootBuildFile
    )
    val bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    val thirdPartyDependenciesList =
      bazelClient.retrieveThirdPartyMavenDepsListForBinary("//:test_oppia")

    assertThat(thirdPartyDependenciesList).doesNotContain("@maven//:androidx_annotation_annotation")
  }

  @Test
  fun testRunCodeCoverage_forSampleTestTarget_returnsCoverageResult() {
    val bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    testBazelWorkspace.initEmptyWorkspace()

    val sourceContent =
      """
      package com.example
      
      class AddNums {
      
          companion object {
              fun sumNumbers(a: Int, b: Int): Any {
                  return if (a == 0 && b == 0) {
                      "Both numbers are zero"
                  } else {
                      a + b
                  }
              }
          }
      }
      """.trimIndent()

    val testContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
              assertEquals(AddNums.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val result = bazelClient.runCoverageForTestTarget(
      "//coverage/test/java/com/example:AddNumsTest"
    )
    val expectedResult = listOf(
      "SF:coverage/main/java/com/example/AddNums.kt",
      "FN:7,com/example/AddNums${'$'}Companion::sumNumbers (II)Ljava/lang/Object;",
      "FN:3,com/example/AddNums::<init> ()V",
      "FNDA:1,com/example/AddNums${'$'}Companion::sumNumbers (II)Ljava/lang/Object;",
      "FNDA:0,com/example/AddNums::<init> ()V",
      "FNF:2",
      "FNH:1",
      "BRDA:7,0,0,1",
      "BRDA:7,0,1,1",
      "BRDA:7,0,2,1",
      "BRDA:7,0,3,1",
      "BRF:4",
      "BRH:4",
      "DA:3,0",
      "DA:7,1",
      "DA:8,1",
      "DA:10,1",
      "LH:3",
      "LF:4",
      "end_of_record"
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCodeCoverage_forNonTestTarget_fails() {
    val bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows<IllegalStateException>() {
      bazelClient.runCoverageForTestTarget("//coverage/test/java/com/example:test")
    }

    // Verify that the underlying Bazel command failed since the test target was not available.
    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no such package")
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

  private fun createAndroidLibrary(artifactName: String, buildFile: File) {
    buildFile.appendText(
      """
      load("@rules_jvm_external//:defs.bzl", "artifact")
      android_library(
          name = "${omitVersionAndReplacePeriodsAndColons(artifactName)}",
          visibility = ["//visibility:public"],
          exports = [
              artifact("$artifactName")
          ],
      )
      """.trimIndent() + "\n"
    )
  }

  private fun omitVersionAndReplacePeriodsAndColons(artifactName: String): String {
    return artifactName.substringBeforeLast(':').replace('.', '_').replace(':', '_')
  }

  private fun createAndroidBinary(
    binaryName: String,
    manifestName: String,
    dependencyName: String
  ) {
    testBazelWorkspace.rootBuildFile.writeText(
      """
      android_binary(
          name = "$binaryName",
          manifest = "$manifestName",
          deps = [
               "$dependencyName"
          ],
      )
      """.trimIndent() + "\n"
    )
  }

  private fun generateCustomJvmTestRuleBazelFile(
    firstFilename: String,
    secondFilename: String
  ): File {
    val firstNewFile = File(tempFolder.root, firstFilename)
    val secondNewFile = File(tempFolder.root, secondFilename)
    firstNewFile.appendText(
      """
      load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")
      def custom_jvm_test_base(name, srcs, deps):
          kt_jvm_test(
              name = name,
              srcs = srcs,
              deps = deps
          )
      """.trimIndent()
    )
    secondNewFile.appendText(
      """
      load("//:$firstFilename", "custom_jvm_test_base")
      def custom_jvm_test(name, srcs, deps):
          custom_jvm_test_base(
              name = name,
              srcs = srcs,
              deps = deps
          )
      """.trimIndent()
    )
    return secondNewFile
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }

  private fun updateBuildFileToUseCustomJvmTestRule(bazelFile: File, buildFile: File) {
    buildFile.prependText(
      "load(\"//:${bazelFile.name}\", \"custom_jvm_test\")\n"
    )
    buildFile.replaceLines("kt_jvm_test(", "custom_jvm_test(")
  }

  private fun File.prependText(line: String) {
    writeLines(listOf(line) + readLines())
  }

  private fun File.replaceLines(find: String, replace: String) {
    writeLines(readLines().map { it.replace(find, replace) })
  }

  private fun File.writeLines(lines: List<String>) {
    writeText(lines.joinToString(separator = "\n"))
  }
}
