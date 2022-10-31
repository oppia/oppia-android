package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
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
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock

/**
 * Tests for [BazelClient].
 *
 * Note that this test executes real commands on the local filesystem & requires Bazel in the local
 * environment.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class BazelClientTest {
  @field:[Rule JvmField] var tempFolder = TemporaryFolder()
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockCommandExecutor: CommandExecutor

  private val commandExecutorBuilder by lazy { initializeExecutorBuilderWithLongProcessWaitTime() }
  private val mockCommandExecutorBuilder by lazy { initializeMockCommandExecutorBuilder() }
  private lateinit var testBazelWorkspace: TestBazelWorkspace

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
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutorBuilder)
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
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutorBuilder)
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
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutorBuilder)
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
  fun testRetrieveTransitiveTestTargets_forBzlFile_returnsRelatedTests() {
    val bazelClient = BazelClient(tempFolder.root)
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
    val bazelClient = BazelClient(tempFolder.root)
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
    val bazelClient = BazelClient(tempFolder.root, mockCommandExecutorBuilder)
    fakeCommandExecutorWithResult(singleLine = "//:FirstTest//:SecondTest")

    val testTargets = bazelClient.retrieveTransitiveTestTargets(listOf("WORKSPACE"))

    assertThat(testTargets).containsExactly("//:FirstTest", "//:SecondTest")
  }

  @Test
  fun testRetrieveMavenDepsList_binaryDependsOnArtifactViaThirdParty_returnsArtifact() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )
    tempFolder.newFile("AndroidManifest.xml")
    createAndroidBinary(
      binaryName = "test_oppia",
      manifestName = "AndroidManifest.xml",
      dependencyName = "//third_party:com_android_support_support-annotations"
    )
    tempFolder.newFolder("third_party")
    val thirdPartyBuild = tempFolder.newFile("third_party/BUILD.bazel")
    createAndroidLibrary(
      artifactName = "com.android.support:support-annotations:28.0.0",
      buildFile = thirdPartyBuild
    )
    val bazelClient = BazelClient(tempFolder.root, commandExecutorBuilder)
    val thirdPartyDependenciesList =
      bazelClient.retrieveThirdPartyMavenDepsListForBinary("//:test_oppia")

    assertThat(thirdPartyDependenciesList)
      .contains("@maven//:com_android_support_support_annotations")
  }

  @Test
  fun testRetrieveMavenDepsList_binaryDependsOnArtifactNotViaThirdParty_doesNotreturnArtifact() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(
      listOf("com.android.support:support-annotations:28.0.0")
    )
    tempFolder.newFile("AndroidManifest.xml")
    createAndroidBinary(
      binaryName = "test_oppia",
      manifestName = "AndroidManifest.xml",
      dependencyName = ":com_android_support_support-annotations"
    )
    tempFolder.newFolder("third_party")
    val thirdPartyBuild = tempFolder.newFile("third_party/BUILD.bazel")
    createAndroidLibrary(
      artifactName = "io.fabric.sdk.android:fabric:1.4.7",
      buildFile = thirdPartyBuild
    )
    createAndroidLibrary(
      artifactName = "com.android.support:support-annotations:28.0.0",
      buildFile = testBazelWorkspace.rootBuildFile
    )
    val bazelClient = BazelClient(tempFolder.root, commandExecutorBuilder)
    val thirdPartyDependenciesList =
      bazelClient.retrieveThirdPartyMavenDepsListForBinary("//:test_oppia")

    assertThat(thirdPartyDependenciesList)
      .doesNotContain("@maven//:com_android_support_support_annotations")
  }

  private fun fakeCommandExecutorWithResult(singleLine: String) {
    // Fake a Bazel command's results to return jumbled results. This has been observed to happen
    // sometimes in CI, but doesn't have a known cause. The utility is meant to de-jumble these in
    // circumstances where they occur, and the only way to guarantee this happens in the test
    // environment is to force the command output.
    `when`(
      mockCommandExecutor.executeCommandInForeground(
        anyOrNull(),
        anyOrNull(),
        stdoutRedirection = anyOrNull(),
        stderrRedirection = anyOrNull()
      )
    ).thenReturn(
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
      load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_test")
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

  private fun initializeExecutorBuilderWithLongProcessWaitTime(): CommandExecutor.Builder {
    val builder = CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder()
    return builder.setProcessTimeout(timeout = 5, timeoutUnit = TimeUnit.MINUTES)
  }

  private fun initializeMockCommandExecutorBuilder(): CommandExecutor.Builder {
    return mock(CommandExecutor.Builder::class.java).also {
      `when`(it.setProcessTimeout(anyLong(), anyOrNull())).thenReturn(it)
      `when`(it.setEnvironmentVariable(anyString(), anyString())).thenReturn(it)
      `when`(it.create(anyOrNull())).thenReturn(mockCommandExecutor)
    }
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
