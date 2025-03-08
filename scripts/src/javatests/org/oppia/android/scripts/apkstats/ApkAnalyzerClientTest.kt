package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.testing.assertThrows
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystemNotFoundException
import java.nio.file.NoSuchFileException
import java.nio.file.ProviderNotFoundException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

/**
 * Tests for [ApkAnalyzerClient].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class ApkAnalyzerClientTest {
  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  private val sdkProperties = AndroidBuildSdkProperties()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }
  private val createdFiles = mutableListOf<File>()

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
    createdFiles.forEach { it.delete() }
  }

  @Test
  fun testComputeDownloadSize_forNonExistentApk_throwsException() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IllegalArgumentException>() {
      apkAnalyzerClient.computeDownloadSize("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
  }

  @Test
  fun testComputeFileSize_nonExistentApk_failsWithError() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IllegalArgumentException>() {
      apkAnalyzerClient.computeFileSize("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
  }

  @Test
  fun testComputeFileSize_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IllegalArgumentException>() {
      apkAnalyzerClient.computeFileSize(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
  }

  @Test
  fun testComputeFileSize_apkWithSomeClassesAndResources_returnsCorrectRawApkSize() {
    val apkFile = createValidApkFile(
      "simple_test.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "resources.arsc" to ByteArray(300),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()
    val rawSize = apkAnalyzerClient.computeFileSize(apkFile.absolutePath)

    // Check that the raw size is approximately right
    assertThat(rawSize).isAtLeast(400)
  }

  @Test
  fun testComputeFileSize_apkWithMoreClassesAndResources_returnsCorrectRawApkSize() {
    val apkFile = createValidApkFile(
      "complex_test.apk",
      mapOf(
        "classes.dex" to ByteArray(1000),
        "classes2.dex" to ByteArray(800),
        "resources.arsc" to ByteArray(500),
        "res/layout/main.xml" to "<layout></layout>".toByteArray(StandardCharsets.UTF_8),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()
    val rawSize = apkAnalyzerClient.computeFileSize(apkFile.absolutePath)

    // Check that the raw size is approximately right
    assertThat(rawSize).isAtLeast(680)
  }

  @Test
  fun testComputeDownloadSize_nonExistentApk_failsWithError() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IllegalArgumentException>() {
      apkAnalyzerClient.computeDownloadSize("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
  }

  @Test
  fun testComputeDownloadSize_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IllegalArgumentException>() {
      apkAnalyzerClient.computeDownloadSize(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
  }

  @Test
  fun testComputeDownloadSize_apkWithSomeClassesResources_returnsEstDownloadSize() {
    val apkFile = createValidApkFile(
      "simple_test.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "resources.arsc" to ByteArray(300),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()
    val downloadSize = apkAnalyzerClient.computeDownloadSize(apkFile.absolutePath)

    val rawSize = apkAnalyzerClient.computeFileSize(apkFile.absolutePath)
    assertThat(downloadSize).isAtMost(rawSize)
    assertThat(downloadSize).isAtLeast(210)
  }

  @Test
  fun testComputeDownloadSize_apkWithMoreClassesResources_returnsEstDownloadSize() {
    val apkFile = createValidApkFile(
      "complex_test.apk",
      mapOf(
        "classes.dex" to ByteArray(1000),
        "classes2.dex" to ByteArray(800),
        "resources.arsc" to ByteArray(500),
        "res/layout/main.xml" to "<layout></layout>".toByteArray(StandardCharsets.UTF_8),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()
    val downloadSize = apkAnalyzerClient.computeDownloadSize(apkFile.absolutePath)

    val rawSize = apkAnalyzerClient.computeFileSize(apkFile.absolutePath)
    assertThat(downloadSize).isAtMost(rawSize)
    assertThat(downloadSize).isAtLeast(290)
  }

  @Test
  fun testComputeFeatures_nonExistentApk_failsWithError() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IOException>() {
      apkAnalyzerClient.computeFeatures("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testComputeFeatures_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IOException>() {
      apkAnalyzerClient.computeFeatures(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testComputeFeatures_apkWithNoFeatures_returnsEmptyList() {
    val apkFile = createValidApkFile(
      "no_features.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandExecutorForBadging()

    val apkAnalyzerClient = createApkAnalyzerClientWithFakeExecutor()
    val features = apkAnalyzerClient.computeFeatures(apkFile.absolutePath)

    assertThat(features).isEmpty()
  }

  @Test
  fun testComputeFeatures_apkWithSomeFeatures_returnsListOfFeatures() {
    val apkFile = createValidApkFile(
      "features.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandExecutorForBadging(
      "feature-group: label='Some Group'",
      "uses-feature: name='android.hardware.camera'",
      "uses-feature: name='android.hardware.faketouch'",
      "uses-feature: name='android.hardware.location.gps'"
    )

    val apkAnalyzerClient = createApkAnalyzerClientWithFakeExecutor()
    val features = apkAnalyzerClient.computeFeatures(apkFile.absolutePath)

    assertThat(features).hasSize(3)
    assertThat(features).contains("android.hardware.camera")
    assertThat(features).contains("android.hardware.faketouch")
    assertThat(features).contains("android.hardware.location.gps")
  }

  @Test
  fun testCompare_firstApk_isNonExistent_failsWithError() {
    val secondApkFile = createValidApkFile(
      "second.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<FileSystemNotFoundException>() {
      apkAnalyzerClient.compare("nonexistent.apk", secondApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("nonexistent.apk")
  }

  @Test
  fun testCompare_secondApk_isNonExistent_failsWithError() {
    val firstApkFile = createValidApkFile(
      "first.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<FileSystemNotFoundException>() {
      apkAnalyzerClient.compare(firstApkFile.absolutePath, "nonexistent.apk")
    }

    assertThat(exception).hasMessageThat().contains("nonexistent.apk")
  }

  @Test
  fun testCompare_firstApk_isInvalid_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val secondApkFile = createValidApkFile(
      "second.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<ProviderNotFoundException>() {
      apkAnalyzerClient.compare(invalidApkFile.absolutePath, secondApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Provider \"jar\" not found")
  }

  @Test
  fun testCompare_secondApk_isInvalid_failsWithError() {
    val firstApkFile = createValidApkFile(
      "first.apk",
      mapOf(
        "classes.dex" to ByteArray(500),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<ProviderNotFoundException>() {
      apkAnalyzerClient.compare(firstApkFile.absolutePath, invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Provider \"jar\" not found")
  }

  @Test
  fun testCompare_complexApks_firstSecondAreSame_returnsEmptyList() {
    val contents = mapOf(
      "classes.dex" to ByteArray(500),
      "resources.arsc" to ByteArray(300),
      "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
    )
    val firstApkFile = createValidApkFile("identical1.apk", contents)
    val secondApkFile = createValidApkFile("identical2.apk", contents)

    val apkAnalyzerClient = createApkAnalyzerClient()
    val differences = apkAnalyzerClient.compare(
      firstApkFile.absolutePath,
      secondApkFile.absolutePath
    )

    assertThat(differences).isEmpty()
  }

  @Test
  fun testCompare_twoDifferentComplexApks_returnsListOfDifferences() {
    val firstContents = mapOf(
      "classes.dex" to ByteArray(500),
      "resources.arsc" to ByteArray(300),
      "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
    )
    val secondContents = mapOf(
      "classes.dex" to ByteArray(600),
      "resources.arsc" to ByteArray(350),
      "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
    )
    val firstApkFile = createValidApkFile("first.apk", firstContents)
    val secondApkFile = createValidApkFile("second.apk", secondContents)

    val apkAnalyzerClient = createApkAnalyzerClient()
    val differences = apkAnalyzerClient.compare(
      firstApkFile.absolutePath,
      secondApkFile.absolutePath
    )

    assertThat(differences).isNotEmpty()
  }

  @Test
  fun testComputeDexReferencesList_nonExistentApk_failsWithError() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<NoSuchFileException>() {
      apkAnalyzerClient.computeDexReferencesList("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("fake.apk")
  }

  @Test
  fun testComputeDexReferencesList_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<ZipException>() {
      apkAnalyzerClient.computeDexReferencesList(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("zip END header not found")
  }

  @Test
  fun testComputeDexReferencesList_apkWithoutDexFiles_returnsEmptyMap() {
    val apkFile = createValidApkFile(
      "no_dex.apk",
      mapOf(
        "resources.arsc" to ByteArray(300),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val apkAnalyzerClient = createApkAnalyzerClient()
    val dexReferences = apkAnalyzerClient.computeDexReferencesList(apkFile.absolutePath)

    assertThat(dexReferences).isEmpty()
  }

  private fun createApkAnalyzerClient(): ApkAnalyzerClient {
    return ApkAnalyzerClient(
      Aapt2Client(
        tempFolder.root.absolutePath,
        sdkProperties.buildToolsVersion,
        scriptBgDispatcher,
        commandExecutor
      )
    )
  }

  private fun createApkAnalyzerClientWithFakeExecutor(): ApkAnalyzerClient {
    return ApkAnalyzerClient(
      Aapt2Client(
        tempFolder.root.absolutePath,
        sdkProperties.buildToolsVersion,
        scriptBgDispatcher,
        fakeCommandExecutor
      )
    )
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }

  /** Creates a valid APK (ZIP) file with the specified contents. */
  private fun createValidApkFile(fileName: String, contents: Map<String, ByteArray>): File {
    val apkFile = File(tempFolder.root, fileName)
    createdFiles.add(apkFile)

    ZipOutputStream(FileOutputStream(apkFile)).use { zipOut ->
      contents.forEach { (path, data) ->
        zipOut.putNextEntry(ZipEntry(path))
        zipOut.write(data)
        zipOut.closeEntry()
      }
    }

    return apkFile
  }

  private fun setupFakeCommandExecutorForBadging(vararg badgingInfo: String) {
    val aapt2Path = File(
      "external/androidsdk", "build-tools/${sdkProperties.buildToolsVersion}/aapt2"
    ).absolutePath

    fakeCommandExecutor.registerHandler(aapt2Path) { _, args, outputStream, _ ->
      if (args.size >= 2 && args[0] == "dump" && args[1] == "badging") {
        badgingInfo.forEach { outputStream.println(it) }
        return@registerHandler 0
      }
      return@registerHandler 1
    }
  }
}
