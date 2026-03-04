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
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }
  private lateinit var briefSummaryFile: File
  private lateinit var fullSummaryFile: File
  private lateinit var mockAab1: File
  private lateinit var mockAab2: File
  private lateinit var mockAab3: File
  private lateinit var mockAab4: File

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

    assertThat(exception).hasMessageThat()
      .contains("The file does not seem to be a valid zip file")
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

    assertThat(exception).hasMessageThat()
      .contains("The file does not seem to be a valid zip file")
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

    assertThat(exception).hasMessageThat()
      .contains("The file does not seem to be a valid zip file")
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

    assertThat(exception).hasMessageThat()
      .contains("The file does not seem to be a valid zip file")
  }

  @Test
  fun testComputeBuildStats_oneProfile_sameAab_returnsCorrectAabStatsWithNoDiffs() {
    val aabFile = createValidAabFile("same_aab.aab")
    setupFakeBundleToolAndAapt2()
    val differencesUtility = createComputeAabDifferencesWithFake()

    val profile = createProfile(
      oldAabFilePath = aabFile.absolutePath,
      newAabFilePath = aabFile.absolutePath
    )
    val stats = differencesUtility.computeBuildStats(profile)

    assertThat(stats.aabStats).hasSize(1)
    assertThat(stats.aabStats).containsKey("dev")
    val aabStats = stats.aabStats.getValue("dev")
    assertThat(aabStats.universalApkStats.fileSizeStats.fileSize.hasDifference()).isFalse()
    assertThat(aabStats.universalApkStats.fileSizeStats.downloadSize.hasDifference()).isFalse()
    assertThat(aabStats.universalApkStats.dexStats.methodCount.hasDifference()).isFalse()
    assertThat(aabStats.universalApkStats.manifestStats.features.hasDifference()).isFalse()
    assertThat(aabStats.universalApkStats.manifestStats.permissions.hasDifference()).isFalse()
    assertThat(aabStats.universalApkStats.assetStats.assets.hasDifference()).isFalse()
  }

  @Test
  fun testComputeBuildStats_oneProfile_diffAabs_returnsCorrectConfigAndDiffStats() {
    val aabFile1 = createValidAabFile("old.aab")
    val aabFile2 = createValidAabFile(
      "new.aab",
      additionalAssets = listOf("assets/new_lesson.json")
    )
    setupFakeBundleToolAndAapt2(
      oldPermissions = listOf("android.permission.INTERNET"),
      newPermissions = listOf("android.permission.INTERNET", "android.permission.CAMERA")
    )
    val differencesUtility = createComputeAabDifferencesWithFake()

    val profile = createProfile(
      oldAabFilePath = aabFile1.absolutePath, newAabFilePath = aabFile2.absolutePath
    )
    val stats = differencesUtility.computeBuildStats(profile)

    assertThat(stats.aabStats).hasSize(1)
    assertThat(stats.aabStats).containsKey("dev")
    val aabStats = stats.aabStats.getValue("dev")
    assertThat(
      aabStats.universalApkStats.manifestStats.permissions.hasDifference()
    ).isTrue()
  }

  @Test
  fun testComputeBuildStats_twoProfiles_sameAabs_returnsCorrectAabStatsWithNoDiffsForEach() {
    val aabFile1 = createValidAabFile("dev.aab")
    val aabFile2 = createValidAabFile("alpha.aab")
    setupFakeBundleToolAndAapt2()
    val differencesUtility = createComputeAabDifferencesWithFake()

    val devProfile = createProfile(
      oldAabFilePath = aabFile1.absolutePath,
      newAabFilePath = aabFile1.absolutePath,
      buildFlavor = "dev"
    )
    val alphaProfile = createProfile(
      oldAabFilePath = aabFile2.absolutePath,
      newAabFilePath = aabFile2.absolutePath,
      buildFlavor = "alpha"
    )
    val stats = differencesUtility.computeBuildStats(devProfile, alphaProfile)

    assertThat(stats.aabStats).hasSize(2)
    assertThat(stats.aabStats).containsKey("dev")
    assertThat(stats.aabStats).containsKey("alpha")
    val devStats = stats.aabStats.getValue("dev")
    val alphaStats = stats.aabStats.getValue("alpha")
    assertThat(devStats.universalApkStats.fileSizeStats.fileSize.hasDifference()).isFalse()
    assertThat(alphaStats.universalApkStats.fileSizeStats.fileSize.hasDifference()).isFalse()
  }

  @Test
  fun testComputeBuildStats_twoProfiles_diffAabs_returnsCorrectConfigAndDiffStatsForEach() {
    val devOld = createValidAabFile("dev_old.aab")
    val devNew = createValidAabFile("dev_new.aab")
    val alphaOld = createValidAabFile("alpha_old.aab")
    val alphaNew = createValidAabFile("alpha_new.aab")
    setupFakeBundleToolAndAapt2(
      oldPermissions = listOf("android.permission.INTERNET"),
      newPermissions = listOf("android.permission.INTERNET", "android.permission.CAMERA")
    )
    setupFakeBundleToolAndAapt2(
      oldPermissions = listOf("android.permission.INTERNET"),
      newPermissions = listOf("android.permission.INTERNET", "android.permission.WRITE_STORAGE")
    )
    val differencesUtility = createComputeAabDifferencesWithFake()

    val devProfile = createProfile(
      oldAabFilePath = devOld.absolutePath,
      newAabFilePath = devNew.absolutePath,
      buildFlavor = "dev"
    )
    val alphaProfile = createProfile(
      oldAabFilePath = alphaOld.absolutePath,
      newAabFilePath = alphaNew.absolutePath,
      buildFlavor = "alpha"
    )
    val stats = differencesUtility.computeBuildStats(devProfile, alphaProfile)

    assertThat(stats.aabStats).hasSize(2)
    val devStats = stats.aabStats.getValue("dev")
    val alphaStats = stats.aabStats.getValue("alpha")
    assertThat(
      devStats.universalApkStats.manifestStats.permissions.hasDifference()
    ).isTrue()
    assertThat(
      alphaStats.universalApkStats.manifestStats.permissions.hasDifference()
    ).isTrue()
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

  @Test
  fun testAabStats_writeSummaryTo_statsAndDiffs_lowItemLimit_reducesOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val statsWithDiffs = createAabStatsWithDiffs()
    statsWithDiffs.writeSummaryTo(printStream, "dev", itemLimit = 1, longSummary = true)

    val output = outputStream.toString()
    assertThat(output).contains("## Dev")
    assertThat(output).contains("### Universal APK")
    assertThat(output).contains("And")
  }

  @Test
  fun testAabStats_writeSummaryTo_statsAndDiffs_longSummaryOff_reducesOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val statsWithDiffs = createAabStatsWithDiffs()
    statsWithDiffs.writeSummaryTo(printStream, "dev", itemLimit = 5, longSummary = false)

    val output = outputStream.toString()
    assertThat(output).contains("## Dev")
    assertThat(output).contains("<details><summary>Expand to see flavor specifics</summary>")
    assertThat(output).contains("<details><summary>Expand to see AAB specifics</summary>")
    assertThat(output).doesNotContain("*Detailed file differences:*")
    assertThat(output).contains("</details></details>")
  }

  @Test
  fun testAabStats_writeSummaryTo_statsWithDiffs_longSummaryOn_lowLimit_printsLimited() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val statsWithDiffs = createAabStatsWithDiffs()
    statsWithDiffs.writeSummaryTo(printStream, "alpha", itemLimit = 2, longSummary = true)

    val output = outputStream.toString()
    assertThat(output).contains("## Alpha")
    assertThat(output).contains("*Detailed file differences:*")
    assertThat(output).doesNotContain("<details>")
    assertThat(output).contains("### Universal APK")
    assertThat(output).contains("### AAB differences")
  }

  @Test
  fun testAabStats_writeSummaryTo_statsWithDiffs_longSummaryOn_highLimit_printsExtensive() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val statsWithDiffs = createAabStatsWithDiffs()
    statsWithDiffs.writeSummaryTo(
      printStream, "dev", itemLimit = Int.MAX_VALUE, longSummary = true
    )

    val output = outputStream.toString()
    assertThat(output).contains("## Dev")
    assertThat(output).contains("*Detailed file differences:*")
    assertThat(output).doesNotContain("<details>")
    assertThat(output).contains("### Universal APK")
    assertThat(output).contains("#### Base APK")
    assertThat(output).doesNotContain("And")
  }

  @Test
  fun testBuildStats_writeSummariesTo_oneProfile_shortSummary_generatesCorrectOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val buildStats = ComputeAabDifferences.BuildStats(
      aabStats = mapOf("dev" to createEmptyAabStats())
    )
    buildStats.writeSummariesTo(printStream, longSummary = false)

    val output = outputStream.toString()
    assertThat(output).contains("# APK & AAB differences analysis")
    assertThat(output).contains("Note that this is a summarized snapshot")
    assertThat(output).contains("## Dev")
    assertThat(output).contains("<details><summary>Expand to see flavor specifics</summary>")
  }

  @Test
  fun testBuildStats_writeSummariesTo_oneProfile_longSummary_generatesCorrectOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val buildStats = ComputeAabDifferences.BuildStats(
      aabStats = mapOf("dev" to createEmptyAabStats())
    )
    buildStats.writeSummariesTo(printStream, longSummary = true)

    val output = outputStream.toString()
    assertThat(output).contains("# APK & AAB differences analysis")
    assertThat(output).doesNotContain("Note that this is a summarized snapshot")
    assertThat(output).contains("## Dev")
    assertThat(output).doesNotContain("<details>")
    assertThat(output).contains("*Detailed file differences:*")
  }

  @Test
  fun testBuildStats_writeSummariesTo_twoProfiles_shortSummary_generatesCorrectOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val buildStats = ComputeAabDifferences.BuildStats(
      aabStats = mapOf(
        "dev" to createEmptyAabStats(),
        "alpha" to createEmptyAabStats()
      )
    )
    buildStats.writeSummariesTo(printStream, longSummary = false)

    val output = outputStream.toString()
    assertThat(output).contains("# APK & AAB differences analysis")
    assertThat(output).contains("## Dev")
    assertThat(output).contains("## Alpha")
    assertThat(output).contains("Note that this is a summarized snapshot")
  }

  @Test
  fun testBuildStats_writeSummariesTo_twoProfiles_longSummary_generatesCorrectOutput() {
    val outputStream = ByteArrayOutputStream()
    val printStream = PrintStream(outputStream)

    val buildStats = ComputeAabDifferences.BuildStats(
      aabStats = mapOf(
        "dev" to createAabStatsWithDiffs(),
        "alpha" to createEmptyAabStats()
      )
    )
    buildStats.writeSummariesTo(printStream, longSummary = true)

    val output = outputStream.toString()
    assertThat(output).contains("# APK & AAB differences analysis")
    assertThat(output).doesNotContain("Note that this is a summarized snapshot")
    assertThat(output).contains("## Dev")
    assertThat(output).contains("## Alpha")
    assertThat(output).doesNotContain("<details>")
  }

  private fun createComputeAabDifferences(): ComputeAabDifferences {
    return ComputeAabDifferences(
      workingDirectoryPath = tempFolder.root.absoluteFile.normalize().path,
      sdkProperties = AndroidBuildSdkProperties(),
      scriptBgDispatcher
    )
  }

  private fun createComputeAabDifferencesWithFake(): ComputeAabDifferences {
    return ComputeAabDifferences(
      workingDirectoryPath = tempFolder.root.absoluteFile.normalize().path,
      sdkProperties = AndroidBuildSdkProperties(),
      scriptBgDispatcher,
      commandExecutor = fakeCommandExecutor
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

  private fun createAabStatsWithDiffs(): ComputeAabDifferences.AabStats {
    val statsWithDiffs = ComputeAabDifferences.ApkConfigurationStats(
      fileSizeStats = ComputeAabDifferences.FileSizeStats(
        fileSize = ComputeAabDifferences.DiffLong(1000, 2000),
        downloadSize = ComputeAabDifferences.DiffLong(500, 800)
      ),
      dexStats = ComputeAabDifferences.DexStats(
        ComputeAabDifferences.DiffLong(5000, 5500)
      ),
      manifestStats = ComputeAabDifferences.ManifestStats(
        features = DiffList(listOf("camera"), listOf("camera", "nfc")),
        permissions = DiffList(
          listOf("android.permission.INTERNET"),
          listOf(
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
          )
        )
      ),
      resourceStats = ComputeAabDifferences.ResourceStats(
        mapOf(
          "string" to DiffList(
            listOf("app_name", "hello"),
            listOf("app_name", "hello", "new_string")
          ),
          "drawable" to DiffList(
            listOf("ic_launcher", "ic_bg"),
            listOf("ic_launcher")
          )
        )
      ),
      assetStats = ComputeAabDifferences.AssetStats(
        DiffList(
          listOf("lesson1.json"),
          listOf("lesson1.json", "lesson2.json", "lesson3.json")
        )
      ),
      completeFileDiff = listOf(
        "1000\t2000\t1000\t/res/layout/activity_main.xml",
        "500\t0\t-500\t/res/drawable/ic_bg.png"
      )
    )
    return ComputeAabDifferences.AabStats(
      universalApkStats = statsWithDiffs,
      mainSplitApkStats = createEmptyApkConfigurationStats(),
      splitApkStats = mapOf(),
      configurationsList = DiffList(listOf("hdpi", "xhdpi"), listOf("hdpi", "xhdpi", "xxhdpi"))
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

  /**
   * Creates a valid AAB-like zip file with the necessary internal structure for bundletool
   * processing.
   */
  private fun createValidAabFile(
    fileName: String,
    additionalAssets: List<String> = listOf()
  ): File {
    val aabFile = File(tempFolder.root, fileName)
    ZipOutputStream(FileOutputStream(aabFile)).use { zipOut ->
      zipOut.putNextEntry(ZipEntry("base/manifest/AndroidManifest.xml"))
      zipOut.write("<manifest/>".toByteArray())
      zipOut.closeEntry()

      zipOut.putNextEntry(ZipEntry("base/dex/classes.dex"))
      zipOut.write(ByteArray(100))
      zipOut.closeEntry()

      additionalAssets.forEach { assetPath ->
        zipOut.putNextEntry(ZipEntry(assetPath))
        zipOut.write("content".toByteArray())
        zipOut.closeEntry()
      }
    }
    return aabFile
  }

  /**
   * Registers fake command handlers for bundletool (java) and aapt2 commands that produce
   * deterministic test output.
   */
  private fun setupFakeBundleToolAndAapt2(
    oldPermissions: List<String> = listOf(),
    newPermissions: List<String> = listOf()
  ) {
    val sdkProperties = AndroidBuildSdkProperties()
    val aapt2Path = File(
      "external/androidsdk", "build-tools/${sdkProperties.buildToolsVersion}/aapt2"
    ).absolutePath

    // Register bundletool (java) handler.
    fakeCommandExecutor.registerHandler("java") { _, args, _, _ ->
      val bundleArgIndex = args.indexOfFirst { it.startsWith("--bundle=") }
      val outputArgIndex = args.indexOfFirst { it.startsWith("--output=") }
      if (bundleArgIndex >= 0 && outputArgIndex >= 0) {
        val outputPath = args[outputArgIndex].substringAfter("--output=")
        val isUniversal = args.any { it == "--mode=universal" }

        // Create a valid APK zip at the output path.
        val apksFile = File(outputPath)
        apksFile.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(apksFile)).use { zipOut ->
          if (isUniversal) {
            zipOut.putNextEntry(ZipEntry("universal.apk"))
            val apkContent = createApkBytes()
            zipOut.write(apkContent)
            zipOut.closeEntry()
          } else {
            zipOut.putNextEntry(ZipEntry("splits/base-master.apk"))
            zipOut.write(createApkBytes())
            zipOut.closeEntry()
          }
        }
        return@registerHandler 0
      }
      return@registerHandler 1
    }

    // Register aapt2 handler.
    fakeCommandExecutor.registerHandler(aapt2Path) { _, args, outputStream, _ ->
      if (args.size >= 3 && args[0] == "dump") {
        val dumpType = args[1]
        val apkPath = args[2]

        // Determine which AAB (old or new) this APK was derived from based on parent path.
        val isNewAab = apkPath.contains("with_changes")

        when (dumpType) {
          "permissions" -> {
            val perms = if (isNewAab) newPermissions else oldPermissions
            perms.forEach { perm ->
              outputStream.println("uses-permission: name='$perm'")
            }
          }
          "resources" -> {
            // Return empty resource dump.
          }
          "badging" -> {
            outputStream.println(
              "package: name='org.oppia.android' versionCode='1' versionName='1.0'"
            )
          }
        }
        return@registerHandler 0
      }
      return@registerHandler 1
    }
  }

  /** Creates a minimal valid APK (zip) byte array with basic structure. */
  private fun createApkBytes(): ByteArray {
    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zipOut ->
      zipOut.putNextEntry(ZipEntry("AndroidManifest.xml"))
      zipOut.write("<manifest/>".toByteArray())
      zipOut.closeEntry()
    }
    return baos.toByteArray()
  }
}
