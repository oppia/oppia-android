package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.apkstats.ComputeAabDifferences.DiffList
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.lang.IllegalStateException

/**
 * Tests for [ComputeAabDifferences].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class ComputeAabDifferencesTest {
  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private lateinit var briefSummaryFile: File
  private lateinit var fullSummaryFile: File
  private lateinit var mockAab1: File
  private lateinit var mockAab2: File
  private lateinit var mockAab3: File
  private lateinit var mockAab4: File

  // TODO(#4971): Finish the tests for this suite.

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Before
  fun setUp() {
    briefSummaryFile = File(tempFolder.root, "brief_summary.txt")
    fullSummaryFile = File(tempFolder.root, "full_summary.txt")

    mockAab1 = File(tempFolder.root, "mock_aab1.aab").apply {
      createNewFile()
      writeText("mock content 1")
    }
    mockAab2 = File(tempFolder.root, "mock_aab2.aab").apply {
      createNewFile()
      writeText("mock content 2")
    }
    mockAab3 = File(tempFolder.root, "mock_aab3.aab").apply {
      createNewFile()
      writeText("mock content 3")
    }
    mockAab4 = File(tempFolder.root, "mock_aab4.aab").apply {
      createNewFile()
      writeText("mock content 4")
    }
  }

  @Test
  fun testComputeBuildStats_forZeroProfiles_returnsEmptyStats() {
    val differencesUtility = createComputeAabDifferences()

    val stats = differencesUtility.computeBuildStats()

    assertThat(stats.aabStats).isEmpty()
  }

  @Test
  fun testComputeBuildStats_forProfileWithMissingFiles_throwsException() {
    val differencesUtility = createComputeAabDifferences()
    val profile = createProfile(oldAabFilePath = "fake.apk", newAabFilePath = "fake.apk")

    val exception = assertThrows<IllegalStateException>() {
      differencesUtility.computeBuildStats(profile)
    }

    assertThat(exception).hasMessageThat().contains("was not found")
  }

  @Test
  fun testMain_noArguments_failsWithError() {
    val exception = assertThrows<ArrayIndexOutOfBoundsException> {
      main()
    }

    assertThat(exception).hasMessageThat().contains("Index 0 out of bounds for length 0")
  }

  @Test
  fun testMain_twoArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(briefSummaryFile.absolutePath, fullSummaryFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_threeArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(briefSummaryFile.absolutePath, fullSummaryFile.absolutePath, "dev")
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_fourArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        mockAab1.absolutePath
      )
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_sixArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        mockAab1.absolutePath,
        mockAab2.absolutePath,
        "extra"
      )
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_sevenArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        mockAab1.absolutePath,
        mockAab2.absolutePath,
        "alpha",
        mockAab3.absolutePath
      )
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_fiveArguments_invalidBeforeAab_failsWithError() {
    val nonExistentFile = File(tempFolder.root, "nonexistent.aab").absolutePath

    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        nonExistentFile,
        mockAab2.absolutePath
      )
    }
    assertThat(exception).hasMessageThat().contains("not found")
  }

  @Test
  fun testMain_fiveArguments_invalidAfterAab_failsWithError() {
    val nonExistentFile = File(tempFolder.root, "nonexistent.aab").absolutePath

    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        mockAab1.absolutePath,
        nonExistentFile
      )
    }

    assertThat(exception).hasMessageThat().contains("The file does not seem to be a valid zip file")
  }

  @Test
  fun testMain_eightArguments_invalidBeforeAabForConfig2_failsWithError() {
    val nonExistentFile = File(tempFolder.root, "nonexistent.aab").absolutePath

    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        mockAab1.absolutePath,
        mockAab2.absolutePath,
        "alpha",
        nonExistentFile,
        mockAab4.absolutePath
      )
    }

    assertThat(exception).hasMessageThat().contains("The file does not seem to be a valid zip file")
  }

  @Test
  fun testMain_eightArguments_invalidAfterAabForConfig2_failsWithError() {
    val nonExistentFile = File(tempFolder.root, "nonexistent.aab").absolutePath

    val exception = assertThrows<IllegalStateException> {
      main(
        briefSummaryFile.absolutePath,
        fullSummaryFile.absolutePath,
        "dev",
        mockAab1.absolutePath,
        mockAab2.absolutePath,
        "alpha",
        mockAab3.absolutePath,
        nonExistentFile
      )
    }

    assertThat(exception).hasMessageThat().contains("The file does not seem to be a valid zip file")
  }

  @Test
  fun testComputeBuildStats_zeroProfiles_returnsEmptyBuildStats() {
    val differencesUtility = createComputeAabDifferences()

    val stats = differencesUtility.computeBuildStats()

    assertThat(stats.aabStats).isEmpty()
  }

  @Test
  fun testComputeBuildStats_oneProfile_invalidBeforeAab_throwsException() {
    val differencesUtility = createComputeAabDifferences()
    val nonExistentFile = File(tempFolder.root, "nonexistent.aab").absolutePath
    val profile = createProfile(
      oldAabFilePath = nonExistentFile, newAabFilePath = mockAab2.absolutePath
    )

    val exception = assertThrows<Exception> {
      differencesUtility.computeBuildStats(profile)
    }

    assertThat(exception).hasMessageThat().contains("not found")
  }

  @Test
  fun testComputeBuildStats_oneProfile_invalidAfterAab_throwsException() {
    val differencesUtility = createComputeAabDifferences()
    val nonExistentFile = File(tempFolder.root, "nonexistent.aab").absolutePath
    val profile = createProfile(
      oldAabFilePath = mockAab1.absolutePath,
      newAabFilePath = nonExistentFile
    )

    val exception = assertThrows<Exception> {
      differencesUtility.computeBuildStats(profile)
    }

    assertThat(exception).hasMessageThat().contains("The file does not seem to be a valid zip file")
  }

  @Test
  fun testAabStats_writeSummaryTo_emptyStats_printsMinimalOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val emptyStats = createEmptyAabStats()
    emptyStats.writeSummaryTo(printStream, "dev", 5, false)

    val output = outputStream.toString()
    assertThat(output).contains("## Dev")
    assertThat(output).contains("### Universal APK")
    assertThat(output).contains("### AAB differences")
    assertThat(output).contains("#### Base APK")
  }

  private fun createComputeAabDifferences(): ComputeAabDifferences {
    return ComputeAabDifferences(
      workingDirectoryPath = tempFolder.root.absoluteFile.normalize().path,
      sdkProperties = AndroidBuildSdkProperties(),
      scriptBgDispatcher
    )
  }

  private fun createProfile(
    oldAabFilePath: String,
    newAabFilePath: String,
    buildFlavor: String = "dev"
  ): ComputeAabDifferences.AabProfile {
    return ComputeAabDifferences.AabProfile(buildFlavor, oldAabFilePath, newAabFilePath)
  }

  private fun createEmptyAabStats(): ComputeAabDifferences.AabStats {
    val emptyApkStats = createEmptyApkConfigurationStats()
    return ComputeAabDifferences.AabStats(
      universalApkStats = emptyApkStats,
      mainSplitApkStats = emptyApkStats,
      splitApkStats = mapOf(),
      configurationsList = DiffList(listOf(), listOf())
    )
  }

  private fun createEmptyApkConfigurationStats(): ComputeAabDifferences.ApkConfigurationStats {
    return ComputeAabDifferences.ApkConfigurationStats(
      fileSizeStats = ComputeAabDifferences.FileSizeStats(
        fileSize = ComputeAabDifferences.DiffLong(0, 0),
        downloadSize = ComputeAabDifferences.DiffLong(0, 0)
      ),
      dexStats = ComputeAabDifferences.DexStats(ComputeAabDifferences.DiffLong(0, 0)),
      manifestStats = ComputeAabDifferences.ManifestStats(
        features = DiffList(listOf(), listOf()),
        permissions = DiffList(listOf(), listOf())
      ),
      resourceStats = ComputeAabDifferences.ResourceStats(mapOf()),
      assetStats = ComputeAabDifferences.AssetStats(DiffList(listOf(), listOf())),
      completeFileDiff = listOf()
    )
  }
}
