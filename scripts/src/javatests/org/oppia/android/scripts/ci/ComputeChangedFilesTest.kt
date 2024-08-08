package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.ChangedFilesBucket
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/**
 * Tests for the compute_changed_files utility.
 */
class ComputeChangedFilesTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }

  private lateinit var commandExecutor: CommandExecutor
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var testGitRepository: TestGitRepository
  private lateinit var pendingOutputStream: ByteArrayOutputStream
  private lateinit var originalStandardOutputStream: OutputStream

  @Before
  fun setUp() {
    commandExecutor = initializeCommandExecutorWithLongProcessWaitTime()
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testGitRepository = TestGitRepository(tempFolder, commandExecutor)

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
    println(testGitRepository.status(checkForGitRepository = false))

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
  fun testUtility_oneArgument_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { main(arrayOf("first")) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_twoArguments_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { main(arrayOf("first", "second")) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_threeArguments_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { main(arrayOf("first", "second", "three")) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_directoryRootDoesNotExist_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_files=false"))
    }

    assertThat(exception).hasMessageThat().contains("Expected 'fake' to be a directory")
  }

  @Test
  fun testUtility_invalid_lastArgument_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_filess=false"))
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected last argument to start with 'compute_all_files='")
  }

  @Test
  fun testUtility_invalid_lastArgumentValue_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_files=blah"))
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected last argument to have 'true' or 'false' passed to it, not: 'blah'")
  }

  @Test
  fun testUtility_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() { runScript(currentHeadHash = "ad") }

    assertThat(exception).hasMessageThat().contains("run from the workspace's root directory")
  }

  @Test
  fun testUtility_emptyWorkspace_returnsNoTargets() {
    // Need to be on a feature branch since the develop branch expects there to be files.
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createEmptyWorkspace()

    val reportedFiles = runScript()

    // An empty workspace should yield no files.
    assertThat(reportedFiles).isEmpty()
  }

  @Test
  fun testUtility_developBranch_returnsAllFiles() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createEmptyWorkspace()

    createAndCommitFile("First", "Second", "Third", subPackage = "app")

    val reportedFiles = runScript()

    // Since the develop branch is checked out, all files should be returned.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("app/First.kt", "app/Second.kt", "app/Third.kt")
  }

  @Test
  fun testUtility_featureBranch_noChanges_returnsNoFiles() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()

    val reportedFiles = runScript()

    assertThat(reportedFiles).isEmpty()
  }

  @Test
  fun testUtility_featureBranch_noChanges_computeAllFiles_returnsAllFiles() {
    initializeEmptyGitRepository()
    createFiles("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()

    val reportedFiles = runScript(computeAllFiles = true)

    // Even though there are no changes, all files should be returned since that was requested via
    // a command argument.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList)
      .containsExactly("app/First.kt", "app/Second.kt", "app/Third.kt")
  }


  @Test
  fun testUtility_featureBranch_fileChange_committed_returnsChangedFile() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()
    changeAndCommitFile("First", subPackage = "app")

    val reportedFiles = runScript()

    // Only the first file should be reported since the file itself was changed & committed.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("app/First.kt")
  }

  @Test
  fun testUtility_featureBranch_fileChange_staged_returnsChangedFile() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()
    changeAndStageFile("First", subPackage = "app")

    val reportedFiles = runScript()

    // Only the first file should be reported since the file itself was changed & staged.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("app/First.kt")
  }

  @Test
  fun testUtility_featureBranch_fileChange_unstaged_returnsChangedFile() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()
    changeAndCommitFile("First", subPackage = "app")

    val reportedFiles = runScript()

    // Only the first file should be reported since the file itself was changed & staged.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("app/First.kt")
  }

  @Test
  fun testUtility_featureBranch_newFile_untracked_returnsChangedFile() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()
    createFiles("NewUntrackedFile", subPackage = "data")

    val reportedFiles = runScript()

    // Only the first file should be reported since the file itself was changed & staged.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("data/NewUntrackedFile.kt")
  }

  @Test
  fun testUtility_featureBranch_deletedFile_committed_returnsNoFiles() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", subPackage = "app")
    switchToFeatureBranch()
    removeAndCommitFile("First", subPackage = "app")

    val reportedFiles = runScript()

    // Removing the file should result in no files being returned (since the file is gone).
    assertThat(reportedFiles).isEmpty()
  }

  @Test
  fun testUtility_featureBranch_movedFile_staged_returnsNewFile() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", subPackage = "app")
    switchToFeatureBranch()
    moveFile(oldFileName = "First", oldSubPackage = "app", newFileName = "RenamedFile", newSubPackage = "domain")

    val reportedFiles = runScript()

    // The file should show up under its new name since moving it is the same as changing it.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("domain/RenamedFile.kt")
  }

  @Test
  fun testUtility_featureBranch_multipleFilesChanged_committed_returnsChangedFiles() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", "Third", subPackage = "app")
    switchToFeatureBranch()
    changeAndCommitFile("First", subPackage = "app")
    changeAndCommitFile("Third", subPackage = "app")

    val reportedFiles = runScript()

    // Changing multiple files independently should be reflected in the script's results.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).containsExactly("app/First.kt", "app/Third.kt")
  }

  @Test
  fun testUtility_developBranch_instrumentationModuleChanged_instrumentationFilesAreIgnored() {
    initializeEmptyGitRepository()
    createFiles("InstrumentationFile", subPackage = "instrumentation/src/javatests/org/oppia/android/instrumentation/player")
    createFiles("Robolectric", subPackage = "instrumentation/src/javatests/org/oppia/android/instrumentation/app")
    createFiles("Third", subPackage = "instrumentation")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).doesNotContain(
      "instrumentation/src/javatests/org/oppia/android/instrumentation/player/InstrumentationFile.kt"
    )
    assertThat(reportedFiles.first().changedFilesList).contains(
      "instrumentation/src/javatests/org/oppia/android/instrumentation/app/Robolectric.kt"
    )
    assertThat(reportedFiles.first().changedFilesList).contains(
      "instrumentation/Third.kt"
    )
  }

  @Test
  fun testUtility_featureBranch_instrumentationModuleChanged_instrumentationFilesAreIgnored() {
    initializeEmptyGitRepository()
    createAndCommitFile("First", "Second", subPackage = "app")
    switchToFeatureBranch()
    createFiles("InstrumentationFile", subPackage = "instrumentation/src/javatests/org/oppia/android/instrumentation/player")
    createFiles("Robolectric", subPackage = "instrumentation/src/javatests/org/oppia/android/instrumentation/app")
    createFiles("Third", subPackage = "instrumentation")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList).doesNotContain(
      "instrumentation/src/javatests/org/oppia/android/instrumentation/player/InstrumentationFile.kt"
    )
    assertThat(reportedFiles.first().changedFilesList).contains(
      "instrumentation/src/javatests/org/oppia/android/instrumentation/app/Robolectric.kt"
    )
    assertThat(reportedFiles.first().changedFilesList).contains(
      "instrumentation/Third.kt"
    )
  }

  @Test
  fun testUtility_appFile_usesAppCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "app")

    val reportedTargets = runScript()

    assertThat(reportedTargets).hasSize(1)
    assertThat(reportedTargets.first().cacheBucketName).isEqualTo("app")
  }

  @Test
  fun testUtility_dataFile_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "data")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_domainFile_usesDomainCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "domain")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().cacheBucketName).isEqualTo("domain")
  }

  @Test
  fun testUtility_instrumentationFile_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "instrumentation")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_scriptsFile_usesScriptsCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "scripts")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().cacheBucketName).isEqualTo("scripts")
  }

  @Test
  fun testUtility_testingFile_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "testing")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_utilityFile_usesGenericCacheName() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("Example", subPackage = "utility")

    val reportedFiles = runScript()

    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().cacheBucketName).isEqualTo("generic")
  }

  @Test
  fun testUtility_testsForMultipleBuckets_correctlyGroupTogether() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("AppFile", subPackage = "app")
    createFiles("DataFile", subPackage = "data")
    createFiles("DomainFile", subPackage = "domain")
    createFiles("InstrumentationFile", subPackage = "instrumentation")
    createFiles("ScriptsFile", subPackage = "scripts")
    createFiles("TestingFile", subPackage = "testing")
    createFiles("UtilityFile", subPackage = "utility")

    val reportedFiles = runScript()

    // Turn the files into a map by cache name in order to guard against potential randomness from
    // the script.
    val fileMap = reportedFiles.associateBy { it.cacheBucketName }
    assertThat(reportedFiles).hasSize(4)
    assertThat(fileMap).hasSize(4)
    assertThat(fileMap).containsKey("app")
    assertThat(fileMap).containsKey("domain")
    assertThat(fileMap).containsKey("generic")
    assertThat(fileMap).containsKey("scripts")
    // Verify that dedicated groups only have their relevant files & the generic group includes all
    // other files.
    assertThat(fileMap["app"]?.changedFilesList).containsExactly("app/AppFile.kt")
    assertThat(fileMap["domain"]?.changedFilesList).containsExactly("domain/DomainFile.kt")
    assertThat(fileMap["generic"]?.changedFilesList)
      .containsExactly(
        "data/DataFile.kt", "instrumentation/InstrumentationFile.kt", "testing/TestingFile.kt",
        "utility/UtilityFile.kt"
      )
    assertThat(fileMap["scripts"]?.changedFilesList)
      .containsExactly("scripts/ScriptsFile.kt")
  }

  @Test
  fun testUtility_appFiles_shardWithSmallPartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("AppFile1", "AppFile2", "AppFile3", subPackage = "app")

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // App module files partition eagerly, so there should be 3 groups. Also, the code below
    // verifies duplicates by ensuring no shards are empty and there are no duplicate files. Note
    // that it's done in this way to be resilient against potential randomness from the script.
    val allFiles = reportedFiles.flatMap { it.changedFilesList }
    assertThat(reportedFiles).hasSize(3)
    assertThat(reportedFiles[0].changedFilesList).isNotEmpty()
    assertThat(reportedFiles[1].changedFilesList).isNotEmpty()
    assertThat(reportedFiles[2].changedFilesList).isNotEmpty()
    assertThat(allFiles).containsExactly("app/AppFile1.kt", "app/AppFile2.kt", "app/AppFile3.kt")
  }

  @Test
  fun testUtility_dataFiles_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("DataFile1", "DataFile2", "DataFile3", subPackage = "data")

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // Data files are partitioned such that they are combined into one partition.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList)
      .containsExactly("data/DataFile1.kt", "data/DataFile2.kt", "data/DataFile3.kt")
  }

  @Test
  fun testUtility_domainFiles_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("DomainFile1", "DomainFile2", "DomainFile3", subPackage = "domain")

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // Domain files are partitioned such that they are combined into one partition.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList)
      .containsExactly("domain/DomainFile1.kt", "domain/DomainFile2.kt", "domain/DomainFile3.kt")
  }

  @Test
  fun testUtility_instrumentationFiles_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles(
      "InstrumentationFile1", "InstrumentationFile2", "InstrumentationFile3",
      subPackage = "instrumentation"
    )

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // Instrumentation files are partitioned such that they are combined into one partition.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList)
      .containsExactly(
        "instrumentation/InstrumentationFile1.kt", "instrumentation/InstrumentationFile2.kt",
        "instrumentation/InstrumentationFile3.kt"
      )
  }

  @Test
  fun testUtility_scriptsFiles_shardWithMediumPartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("ScriptsFile1", "ScriptsFile2", "ScriptsFile3", subPackage = "scripts")

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // See app module file above for specifics. Scripts files are medium partitioned which means 3
    // files will be split into two partitions.
    val allFiles = reportedFiles.flatMap { it.changedFilesList }
    assertThat(reportedFiles).hasSize(2)
    assertThat(reportedFiles[0].changedFilesList).isNotEmpty()
    assertThat(reportedFiles[1].changedFilesList).isNotEmpty()
    assertThat(allFiles)
      .containsExactly("scripts/ScriptsFile1.kt", "scripts/ScriptsFile2.kt", "scripts/ScriptsFile3.kt")
  }

  @Test
  fun testUtility_testingFiles_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("TestingFile1", "TestingFile2", "TestingFile3", subPackage = "testing")

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // Testing files are partitioned such that they are combined into one partition.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList)
      .containsExactly("testing/TestingFile1.kt", "testing/TestingFile2.kt", "testing/TestingFile3.kt")
  }

  @Test
  fun testUtility_utilityFiles_shardWithLargePartitions() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("UtilityFile1", "UtilityFile2", "UtilityFile3", subPackage = "utility")

    val reportedFiles = runScriptWithShardLimits(
      maxFileCountPerLargeShard = 3,
      maxFileCountPerMediumShard = 2,
      maxFileCountPerSmallShard = 1
    )

    // Utility tests are partitioned such that they are combined into one partition.
    assertThat(reportedFiles).hasSize(1)
    assertThat(reportedFiles.first().changedFilesList)
      .containsExactly("utility/UtilityFile1.kt", "utility/UtilityFile2.kt", "utility/UtilityFile3.kt")
  }

  @Test
  fun testUtility_singleShard_fileOutputIncludesHumanReadablePrefix() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("ExampleFile", subPackage = "app")

    val generatedLines = runScriptWithTextOutput()

    assertThat(generatedLines).hasSize(1)
    assertThat(generatedLines.first()).startsWith("app-shard0")
  }

  @Test
  fun testUtility_twoShards_computesFilesForBothShards() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("AppFile1", "AppFile2", "AppFile3", subPackage = "app")
    createFiles("ScriptsFile1", "ScriptsFile2", subPackage = "scripts")

    val reportedFiles = runScript()

    // Turn the files into a map by cache name in order to guard against potential randomness from
    // the script.
    val filetMap = reportedFiles.associateBy { it.cacheBucketName }
    assertThat(reportedFiles).hasSize(2)
    assertThat(filetMap).hasSize(2)
    assertThat(filetMap).containsKey("app")
    assertThat(filetMap).containsKey("scripts")
    assertThat(filetMap["app"]?.changedFilesList)
      .containsExactly("app/AppFile1.kt", "app/AppFile2.kt", "app/AppFile3.kt")
    assertThat(filetMap["scripts"]?.changedFilesList)
      .containsExactly("scripts/ScriptsFile1.kt", "scripts/ScriptsFile2.kt")
  }

  @Test
  fun testUtility_multipleShards_fileOutputIncludesHumanReadablePrefixForEachShard() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createFiles("AppFile", subPackage = "app")
    createFiles("ScriptsFile", subPackage = "scripts")

    // The sorting here counteracts the intentional randomness from the script.
    val generatedLines = runScriptWithTextOutput().sorted()

    assertThat(generatedLines).hasSize(2)
    assertThat(generatedLines[0]).matches("^app-shard[0-3];.+?$")
    assertThat(generatedLines[1]).matches("^scripts-shard[0-3];.+?\$")
  }

  private fun runScriptWithTextOutput(
    currentHeadHash: String = computeMergeBase("develop"),
    computeAllFiles: Boolean = false
  ): List<String> {
    val outputLog = tempFolder.newFile("output.log")
    main(
      arrayOf(
        tempFolder.root.absolutePath,
        outputLog.absolutePath,
        currentHeadHash,
        "compute_all_files=$computeAllFiles"
      )
    )
    return outputLog.readLines()
  }

  /**
   * Runs the compute_affected_files utility & returns all of the output lines. Note that the output
   * here is that which is saved directly to the output file, not debug lines printed to the
   * console.
   */
  private fun runScript(
    currentHeadHash: String = computeMergeBase("develop"),
    computeAllFiles: Boolean = false
  ): List<ChangedFilesBucket> {
    return parseOutputLogLines(runScriptWithTextOutput(currentHeadHash, computeAllFiles))
  }

  private fun runScriptWithShardLimits(
    baseBranch: String = "develop",
    maxFileCountPerLargeShard: Int,
    maxFileCountPerMediumShard: Int,
    maxFileCountPerSmallShard: Int
  ): List<ChangedFilesBucket> {
    val outputLog = tempFolder.newFile("output.log")
    val currentHeadHash = computeMergeBase(baseBranch)

    // Note that main() can't be used since the shard counts need to be overwritten. Dagger would
    // be a nicer means to do this, but it's not set up currently for scripts.
    ComputeChangedFiles(
      scriptBgDispatcher,
      maxFileCountPerLargeShard = maxFileCountPerLargeShard,
      maxFileCountPerMediumShard = maxFileCountPerMediumShard,
      maxFileCountPerSmallShard = maxFileCountPerSmallShard,
      commandExecutor = commandExecutor
    ).compute(
      pathToRoot = tempFolder.root.absolutePath,
      pathToOutputFile = outputLog.absolutePath,
      baseCommit = currentHeadHash,
      computeAllFilesSetting = false
    )

    return parseOutputLogLines(outputLog.readLines())
  }

  private fun parseOutputLogLines(outputLogLines: List<String>): List<ChangedFilesBucket> {
    return outputLogLines.map {
      ChangedFilesBucket.getDefaultInstance().mergeFromCompressedBase64(it.split(";")[1])
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

  private fun createFiles(vararg fileNames: String, subPackage: String): List<File> {
    createEmptyWorkspace()
    if (!File(tempFolder.root, subPackage).exists()) {
      tempFolder.newFolder(subPackage)
    }
    return fileNames.map { fileName ->
      tempFolder.newFile("$subPackage/$fileName.kt")
    }
  }

  private fun createAndCommitFile(vararg fileNames: String, subPackage: String) {
    val createdFiles = createFiles(fileNames = fileNames, subPackage = subPackage)

    testGitRepository.stageFilesForCommit(createdFiles)
    testGitRepository.commit(message = "Introduce files.")
  }

  private fun changeFile(fileName: String, subPackage: String): File {
    val file = File(tempFolder.root, "$subPackage/$fileName.kt")
    file.appendText(";") // Add a character to change the file.
    return file
  }

  private fun changeAndStageFile(fileName: String, subPackage: String) {
    val file = changeFile(fileName, subPackage)
    testGitRepository.stageFileForCommit(file)
  }

  private fun changeAndCommitFile(fileName: String, subPackage: String) {
    changeAndStageFile(fileName, subPackage)
    testGitRepository.commit(message = "Modified file $fileName")
  }

  private fun moveFile(oldFileName: String, oldSubPackage: String, newFileName: String, newSubPackage: String) {
    val oldFilePath = File(tempFolder.root, "$oldSubPackage/$oldFileName.kt")
    val newFilePath = File(tempFolder.root, "$newSubPackage/$newFileName.kt")

    oldFilePath.copyTo(newFilePath)
    oldFilePath.delete()

    testGitRepository.stageFileForCommit(newFilePath)

    testGitRepository.commit(message = "Move file from $oldFilePath to $newFilePath")
  }

  private fun removeAndCommitFile(fileName: String, subPackage: String) {
    val file = File(tempFolder.root, "$subPackage/$fileName.kt")
    testGitRepository.removeFileForCommit(file)
    testGitRepository.commit(message = "Remove file $fileName")
  }

  private fun switchToFeatureBranch() {
    testGitRepository.checkoutNewBranch("introduce-feature")
  }

  private fun computeMergeBase(referenceBranch: String): String =
    GitClient(tempFolder.root, referenceBranch, commandExecutor).branchMergeBase

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
