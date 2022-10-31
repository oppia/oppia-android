package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.proto.AffectedTestsBucket
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
    testGitRepository =
      TestGitRepository(tempFolder, CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder())

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
  fun testUtility_twoArguments_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) { main(arrayOf("first", "second")) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_threeArguments_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) {
      main(arrayOf("first", "second", "third"))
    }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_directoryRootDoesNotExist_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_tests=false"))
    }

    assertThat(exception).hasMessageThat().contains("Expected 'fake' to be a directory")
  }

  @Test
  fun testUtility_invalid_lastArgument_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_testss=false"))
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected last argument to start with 'compute_all_tests='")
  }

  @Test
  fun testUtility_invalid_lastArgumentValue_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_tests=blah"))
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected last argument to have 'true' or 'false' passed to it, not: 'blah'")
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
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")

    val reportedTargets = runScript()

    // Since the develop branch is checked out, all test targets should be returned.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly(
      "//app:FirstTest", "//app:SecondTest", "//app:ThirdTest"
    )
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_noChanges_returnsNoTargets() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()

    val reportedTargets = runScript()

    // No changes are on the feature branch, so no targets should be returned.
    assertThat(reportedTargets).isEmpty()
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_noChanges_computeAllTargets_returnsAllTests() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()

    val reportedTargets = runScript(computeAllTargets = true)

    // Even though there are no changes, all targets should be returned since that was requested via
    // a command argument.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly(
      "//app:FirstTest", "//app:SecondTest", "//app:ThirdTest"
    )
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_testChange_committed_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeAndCommitTestFile("FirstTest")

    val reportedTargets = runScript()

    // Only the first test should be reported since the test file itself was changed & committed.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly("//app:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_testChange_staged_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeAndStageTestFile("FirstTest")

    val reportedTargets = runScript()

    // Only the first test should be reported since the test file itself was changed & staged.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly("//app:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_testChange_unstaged_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeTestFile("FirstTest")

    val reportedTargets = runScript()

    // The first test should still be reported since it was changed (even though it wasn't staged).
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly("//app:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_newTest_untracked_returnsNewTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    // A separate subpackage is needed to avoid unintentionally changing the BUILD file used by the
    // other already-committed tests.
    createBasicTests("NewUntrackedTest", subpackage = "data")

    val reportedTargets = runScript()

    // The new test should still be reported since it was changed (even though it wasn't staged).
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly(
      "//data:NewUntrackedTest"
    )
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_dependencyChange_committed_returnsTestTarget() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", withGeneratedDependencies = true)
    switchToFeatureBranch()
    changeAndCommitDependencyFileForTest("FirstTest")

    val reportedTargets = runScript()

    // The first test should be reported since its dependency was changed.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).containsExactly("//app:FirstTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_commonDepChange_committed_returnsTestTargets() {
    initializeEmptyGitRepository()
    val targetName = createAndCommitLibrary("CommonDependency")
    createAndCommitBasicAppTests("FirstTest", withGeneratedDependencies = true)
    createAndCommitBasicAppTests("SecondTest", "ThirdTest", withExtraDependency = targetName)
    switchToFeatureBranch()
    changeAndCommitLibrary("CommonDependency")

    val reportedTargets = runScript()

    // The two tests with a common dependency should be reported since that dependency was changed.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//app:SecondTest", "//app:ThirdTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_buildFileChange_committed_returnsRelatedTargets() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest")
    switchToFeatureBranch()
    createAndCommitBasicAppTests("ThirdTest")

    val reportedTargets = runScript()

    // Introducing a fourth test requires changing the common BUILD file which leads to the other
    // tests becoming affected.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//app:FirstTest", "//app:SecondTest", "//app:ThirdTest")
  }

  @Test
  fun testUtility_bazelWorkspace_featureBranch_deletedTest_committed_returnsNoTargets() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest")
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
    createAndCommitBasicAppTests("FirstTest")
    switchToFeatureBranch()
    moveTest(oldTestName = "FirstTest", newTestName = "RenamedTest", newSubpackage = "domain")

    val reportedTargets = runScript()

    // The test should show up under its new name since moving it is the same as changing it.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//domain:RenamedTest")
  }

  @Test
  fun testUtility_featureBranch_multipleTargetsChanged_committed_returnsAffectedTests() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest", "ThirdTest")
    switchToFeatureBranch()
    changeAndCommitTestFile("FirstTest")
    changeAndCommitTestFile("ThirdTest")

    val reportedTargets = runScript()

    // Changing multiple tests independently should be reflected in the script's results.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//app:FirstTest", "//app:ThirdTest")
  }

  @Test
  fun testUtility_featureBranch_instrumentationModuleChanged_instrumentationTargetsAreIgnored() {
    initializeEmptyGitRepository()
    createAndCommitBasicAppTests("FirstTest", "SecondTest")
    switchToFeatureBranch()
    createBasicTests(
      "InstrumentationTest",
      subpackage = "instrumentation.src.javatests.org.oppia.android.instrumentation.player"
    )
    createBasicTests(
      "RobolectricTest",
      subpackage = "instrumentation.src.javatests.org.oppia.android.instrumentation.app"
    )
    createBasicTests("ThirdTest", subpackage = "instrumentation")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).doesNotContain(
      "//instrumentation/src/javatests/org/oppia/android/instrumentation/player:InstrumentationTest"
    )
    assertThat(reportedTargets.first().affectedTestTargetsList).contains(
      "//instrumentation/src/javatests/org/oppia/android/instrumentation/app:RobolectricTest"
    )
  }

  @Test
  fun testUtility_developBranch_instrumentationModuleChanged_instrumentationTargetsAreIgnored() {
    initializeEmptyGitRepository()
    createBasicTests(
      "InstrumentationTest",
      subpackage = "instrumentation.src.javatests.org.oppia.android.instrumentation.player"
    )
    createBasicTests(
      "RobolectricTest",
      subpackage = "instrumentation.src.javatests.org.oppia.android.instrumentation.app"
    )
    createBasicTests("ThirdTest", subpackage = "instrumentation")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList).doesNotContain(
      "//instrumentation/src/javatests/org/oppia/android/instrumentation/player:InstrumentationTest"
    )
    assertThat(reportedTargets.first().affectedTestTargetsList).contains(
      "//instrumentation/src/javatests/org/oppia/android/instrumentation/app:RobolectricTest"
    )
  }

  @Test
  fun testUtility_appTest_usesAppCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "app")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("app")
  }

  @Test
  fun testUtility_dataTest_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "data")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_domainTest_usesDomainCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "domain")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("domain")
  }

  @Test
  fun testUtility_instrumentationTest_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "instrumentation")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_scriptsTest_usesScriptsCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "scripts")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("scripts")
  }

  @Test
  fun testUtility_testingTest_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "testing")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_utilityTest_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "utility")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_testsForMultipleBuckets_correctlyGroupTogether() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("AppTest", subpackage = "app")
    createBasicTests("DataTest", subpackage = "data")
    createBasicTests("DomainTest", subpackage = "domain")
    createBasicTests("InstrumentationTest", subpackage = "instrumentation")
    createBasicTests("ScriptsTest", subpackage = "scripts")
    createBasicTests("TestingTest", subpackage = "testing")
    createBasicTests("UtilityTest", subpackage = "utility")

    val reportedTargets = runScript()

    // Turn the targets into a map by cache name in order to guard against potential randomness from
    // the script.
    val targetMap = reportedTargets.associateBy { it.cacheBucketName }
    assertThat(reportedTargets).hasSize(4)
    assertThat(targetMap).hasSize(4)
    assertThat(targetMap).containsKey("app")
    assertThat(targetMap).containsKey("domain")
    assertThat(targetMap).containsKey("generic")
    assertThat(targetMap).containsKey("scripts")
    // Verify that dedicated groups only have their relevant tests & the generic group includes all
    // other tests.
    assertThat(targetMap["app"]?.affectedTestTargetsList).containsExactly("//app:AppTest")
    assertThat(targetMap["domain"]?.affectedTestTargetsList).containsExactly("//domain:DomainTest")
    assertThat(targetMap["generic"]?.affectedTestTargetsList)
      .containsExactly(
        "//data:DataTest", "//instrumentation:InstrumentationTest", "//testing:TestingTest",
        "//utility:UtilityTest"
      )
    assertThat(targetMap["scripts"]?.affectedTestTargetsList)
      .containsExactly("//scripts:ScriptsTest")
  }

  @Test
  fun testUtility_appTests_shardWithSmallPartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("AppTest1", "AppTest2", "AppTest3", subpackage = "app")

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // App module tests partition eagerly, so there should be 3 groups. Also, the code below
    // verifies duplicates by ensuring no shards are empty and there are no duplicate tests. Note
    // that it's done in this way to be resilient against potential randomness from the script.
    val allTests = reportedTargets.flatMap { it.affectedTestTargetsList }
    assertThat(reportedTargets).hasSize(3)
    assertThat(reportedTargets[0].affectedTestTargetsList).isNotEmpty()
    assertThat(reportedTargets[1].affectedTestTargetsList).isNotEmpty()
    assertThat(reportedTargets[2].affectedTestTargetsList).isNotEmpty()
    assertThat(allTests).containsExactly("//app:AppTest1", "//app:AppTest2", "//app:AppTest3")
  }

  @Test
  fun testUtility_dataTests_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("DataTest1", "DataTest2", "DataTest3", subpackage = "data")

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // Data tests are partitioned such that they are combined into one partition.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//data:DataTest1", "//data:DataTest2", "//data:DataTest3")
  }

  @Test
  fun testUtility_domainTests_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("DomainTest1", "DomainTest2", "DomainTest3", subpackage = "domain")

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // Domain tests are partitioned such that they are combined into one partition.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//domain:DomainTest1", "//domain:DomainTest2", "//domain:DomainTest3")
  }

  @Test
  fun testUtility_instrumentationTests_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests(
      "InstrumentationTest1", "InstrumentationTest2", "InstrumentationTest3",
      subpackage = "instrumentation"
    )

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // Instrumentation tests are partitioned such that they are combined into one partition.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly(
        "//instrumentation:InstrumentationTest1", "//instrumentation:InstrumentationTest2",
        "//instrumentation:InstrumentationTest3"
      )
  }

  @Test
  fun testUtility_scriptsTests_shardWithMediumPartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ScriptsTest1", "ScriptsTest2", "ScriptsTest3", subpackage = "scripts")

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // See app module test above for specifics. Scripts tests are medium partitioned which means 3
    // tests will be split into two partitions.
    val allTests = reportedTargets.flatMap { it.affectedTestTargetsList }
    assertThat(reportedTargets).hasSize(2)
    assertThat(reportedTargets[0].affectedTestTargetsList).isNotEmpty()
    assertThat(reportedTargets[1].affectedTestTargetsList).isNotEmpty()
    assertThat(allTests)
      .containsExactly("//scripts:ScriptsTest1", "//scripts:ScriptsTest2", "//scripts:ScriptsTest3")
  }

  @Test
  fun testUtility_testingTests_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("TestingTest1", "TestingTest2", "TestingTest3", subpackage = "testing")

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // Testing tests are partitioned such that they are combined into one partition.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//testing:TestingTest1", "//testing:TestingTest2", "//testing:TestingTest3")
  }

  @Test
  fun testUtility_utilityTests_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("UtilityTest1", "UtilityTest2", "UtilityTest3", subpackage = "utility")

    val reportedTargets = runScriptWithShardLimits(
      maxTestCountPerLargeShard = 3, maxTestCountPerMediumShard = 2, maxTestCountPerSmallShard = 1
    )

    // Utility tests are partitioned such that they are combined into one partition.
    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().affectedTestTargetsList)
      .containsExactly("//utility:UtilityTest1", "//utility:UtilityTest2", "//utility:UtilityTest3")
  }

  @Test
  fun testUtility_singleShard_testOutputIncludesHumanReadablePrefix() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("ExampleTest", subpackage = "app")

    val generatedLines = runScriptWithTextOutput()

    assertThat(generatedLines).hasSize(1)
    assertThat(generatedLines.first()).startsWith("app-shard0")
  }

  @Test
  fun testUtility_multipleShards_testOutputIncludesHumanReadablePrefixForEachShard() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("AppTest", subpackage = "app")
    createBasicTests("ScriptsTest", subpackage = "scripts")

    // The sorting here counteracts the intentional randomness from the script.
    val generatedLines = runScriptWithTextOutput().sorted()

    assertThat(generatedLines).hasSize(2)
    assertThat(generatedLines[0]).matches("^app-shard[0-3];.+?$")
    assertThat(generatedLines[1]).matches("^scripts-shard[0-3];.+?\$")
  }

  @Test
  fun testUtility_twoShards_computesTestsForBothShards() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createBasicTests("AppTest1", "AppTest2", "AppTest3", subpackage = "app")
    createBasicTests("ScriptsTest1", "ScriptsTest2", subpackage = "scripts")

    val reportedTargets = runScript()

    // Turn the targets into a map by cache name in order to guard against potential randomness from
    // the script.
    val targetMap = reportedTargets.associateBy { it.cacheBucketName }
    assertThat(reportedTargets).hasSize(2)
    assertThat(targetMap).hasSize(2)
    assertThat(targetMap).containsKey("app")
    assertThat(targetMap).containsKey("scripts")
    assertThat(targetMap["app"]?.affectedTestTargetsList)
      .containsExactly("//app:AppTest1", "//app:AppTest2", "//app:AppTest3")
    assertThat(targetMap["scripts"]?.affectedTestTargetsList)
      .containsExactly("//scripts:ScriptsTest1", "//scripts:ScriptsTest2")
  }

  private fun runScriptWithTextOutput(computeAllTargets: Boolean = false): List<String> {
    val outputLog = tempFolder.newFile("output.log")
    main(
      arrayOf(
        tempFolder.root.absolutePath, outputLog.absolutePath, "develop",
        "compute_all_tests=$computeAllTargets"
      )
    )
    return outputLog.readLines()
  }

  /**
   * Runs the compute_affected_tests utility & returns all of the output lines. Note that the output
   * here is that which is saved directly to the output file, not debug lines printed to the
   * console.
   */
  private fun runScript(computeAllTargets: Boolean = false): List<AffectedTestsBucket> {
    return parseOutputLogLines(runScriptWithTextOutput(computeAllTargets = computeAllTargets))
  }

  private fun runScriptWithShardLimits(
    maxTestCountPerLargeShard: Int,
    maxTestCountPerMediumShard: Int,
    maxTestCountPerSmallShard: Int
  ): List<AffectedTestsBucket> {
    val outputLog = tempFolder.newFile("output.log")

    // Note that main() can't be used since the shard counts need to be overwritten. Dagger would
    // be a nicer means to do this, but it's not set up currently for scripts.
    ComputeAffectedTests(
      maxTestCountPerLargeShard = maxTestCountPerLargeShard,
      maxTestCountPerMediumShard = maxTestCountPerMediumShard,
      maxTestCountPerSmallShard = maxTestCountPerSmallShard
    ).compute(
      pathToRoot = tempFolder.root.absolutePath,
      pathToOutputFile = outputLog.absolutePath,
      baseDevelopBranchReference = "develop",
      computeAllTestsSetting = false
    )

    return parseOutputLogLines(outputLog.readLines())
  }

  private fun parseOutputLogLines(outputLogLines: List<String>): List<AffectedTestsBucket> {
    return outputLogLines.map {
      AffectedTestsBucket.getDefaultInstance().mergeFromCompressedBase64(it.split(";")[1])
    }
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
   * @param subpackage the subpackage under which the tests should be created
   * @param withGeneratedDependencies whether each test should have a corresponding test dependency
   *     generated
   * @param withExtraDependency if present, an extra library dependency that should be added to each
   *     test
   */
  private fun createBasicTests(
    vararg testNames: String,
    subpackage: String?,
    withGeneratedDependencies: Boolean = false,
    withExtraDependency: String? = null
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

  private fun createAndCommitBasicAppTests(
    vararg testNames: String,
    withGeneratedDependencies: Boolean = false,
    withExtraDependency: String? = null
  ) {
    val changedFiles = createBasicTests(
      *testNames,
      withGeneratedDependencies = withGeneratedDependencies,
      withExtraDependency = withExtraDependency,
      subpackage = "app"
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
    val libFile = testBazelWorkspace.retrieveLibraryFile("//:$name")
    libFile.appendText(";") // Add a character to change the file.
    testGitRepository.stageFileForCommit(libFile)
    testGitRepository.commit(message = "Modified library $name")
  }
}
