package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.testing.assertThrows
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Tests for [BundleToolClient].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class BundleToolClientTest {
  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testBuildUniversalApk_forNonExistentAab_throwsException() {
    val bundleToolClient =
      BundleToolClient(tempFolder.root.absolutePath, scriptBgDispatcher, commandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      bundleToolClient.buildUniversalApk("fake.aab", "fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("was not found")
  }

  @Test
  fun testBuildApks_nonExistentInputAab_failsWithError() {
    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      commandExecutor
    )
    val outputApksPath = "${tempFolder.root.absolutePath}/output.apks"
    val outputApkDirPath = "${tempFolder.root.absolutePath}/apks"

    val exception = assertThrows<IllegalStateException> {
      bundleToolClient.buildApks(
        "nonexistent.aab",
        outputApksPath,
        outputApkDirPath
      )
    }

    assertThat(exception).hasMessageThat().contains("was not found")
  }

  @Test
  fun testBuildApks_invalidInputAab_failsWithError() {
    val invalidAabFile = tempFolder.newFile("invalid.aab")
    invalidAabFile.writeText("This is not a valid AAB file")
    val outputApksPath = "${tempFolder.root.absolutePath}/output.apks"
    val outputApkDirPath = "${tempFolder.root.absolutePath}/apks"

    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      commandExecutor
    )

    val exception = assertThrows<IllegalStateException> {
      bundleToolClient.buildApks(
        invalidAabFile.absolutePath,
        outputApksPath,
        outputApkDirPath
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("The file does not seem to be a valid zip file.")
  }

  @Test
  fun testBuildApks_aabWithOneConfiguration_returnsListWithSingleApkFile() {
    val fakeCommandExecutor = FakeCommandExecutor()
    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      fakeCommandExecutor
    )

    val inputAabPath = "${tempFolder.root.absolutePath}/app.aab"
    val outputApksPath = "${tempFolder.root.absolutePath}/output.apks"
    val outputApkDirPath = "${tempFolder.root.absolutePath}/apks"
    tempFolder.newFolder("apks")

    val apksFile = tempFolder.newFile("output.apks")
    val zipOutputStream = ZipOutputStream(apksFile.outputStream())
    zipOutputStream.putNextEntry(ZipEntry("splits/base-mdpi.apk"))
    zipOutputStream.closeEntry()
    zipOutputStream.close()

    setupFakeCommandExecutorForBuildApks(
      fakeCommandExecutor,
      inputAabPath,
      outputApksPath
    )

    val result = bundleToolClient.buildApks(
      inputAabPath,
      outputApksPath,
      outputApkDirPath
    )

    assertThat(result).hasSize(1)
    assertThat(result[0].name).isEqualTo("base-mdpi.apk")
  }

  @Test
  fun testBuildApks_aabWithMultipleConfigurations_returnsListWithAllApkFiles() {
    val fakeCommandExecutor = FakeCommandExecutor()
    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      fakeCommandExecutor
    )

    val inputAabPath = "${tempFolder.root.absolutePath}/app.aab"
    val outputApksPath = "${tempFolder.root.absolutePath}/output.apks"
    val outputApkDirPath = "${tempFolder.root.absolutePath}/apks"
    tempFolder.newFolder("apks")

    val apksFile = tempFolder.newFile("output.apks")
    val zipOutputStream = ZipOutputStream(apksFile.outputStream())
    val entries = listOf(
      "splits/base-mdpi.apk",
      "splits/base-hdpi.apk",
      "splits/base-xhdpi.apk",
      "splits/base-xxhdpi.apk"
    )
    entries.forEach {
      zipOutputStream.putNextEntry(ZipEntry(it))
      zipOutputStream.closeEntry()
    }
    zipOutputStream.close()

    setupFakeCommandExecutorForBuildApks(
      fakeCommandExecutor,
      inputAabPath,
      outputApksPath
    )

    val result = bundleToolClient.buildApks(
      inputAabPath,
      outputApksPath,
      outputApkDirPath
    )

    assertThat(result).hasSize(4)
    assertThat(result.map { it.name }).containsExactly(
      "base-mdpi.apk",
      "base-hdpi.apk",
      "base-xhdpi.apk",
      "base-xxhdpi.apk"
    )
  }

  @Test
  fun testBuildUniversalApk_nonExistentInputAab_failsWithError() {
    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      commandExecutor
    )
    val outputApkPath = "${tempFolder.root.absolutePath}/universal.apk"

    val exception = assertThrows<IllegalStateException> {
      bundleToolClient.buildUniversalApk("nonexistent.aab", outputApkPath)
    }

    assertThat(exception).hasMessageThat().contains("was not found")
  }

  @Test
  fun testBuildUniversalApk_invalidInputAab_failsWithError() {
    val invalidAabFile = tempFolder.newFile("invalid.aab")
    invalidAabFile.writeText("This is not a valid AAB file")
    val outputApkPath = "${tempFolder.root.absolutePath}/universal.apk"

    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      commandExecutor
    )

    val exception = assertThrows<IllegalStateException> {
      bundleToolClient.buildUniversalApk(invalidAabFile.absolutePath, outputApkPath)
    }

    assertThat(exception).hasMessageThat()
      .contains("The file does not seem to be a valid zip file.")
  }

  @Test
  fun testBuildUniversalApk_aabWithOneConfiguration_returnsValidUniversalApkFile() {
    val fakeCommandExecutor = FakeCommandExecutor()
    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      fakeCommandExecutor
    )

    val inputAabPath = "${tempFolder.root.absolutePath}/app.aab"
    val outputApkPath = "${tempFolder.root.absolutePath}/universal.apk"

    val apksFile = tempFolder.newFile("universal.apk.apks")
    val zipOutputStream = ZipOutputStream(apksFile.outputStream())
    zipOutputStream.putNextEntry(ZipEntry("universal.apk"))
    zipOutputStream.write("fake apk content".toByteArray())
    zipOutputStream.closeEntry()
    zipOutputStream.close()

    setupFakeCommandExecutorForBuildUniversalApk(
      fakeCommandExecutor,
      inputAabPath,
      "$outputApkPath.apks"
    )

    val result = bundleToolClient.buildUniversalApk(
      inputAabPath,
      outputApkPath
    )

    assertThat(result.exists()).isTrue()
    assertThat(result.absolutePath).isEqualTo(outputApkPath)
    assertThat(result.readText()).isEqualTo("fake apk content")
  }

  @Test
  fun testBuildUniversalApk_aabWithMultipleConfigurations_returnsValidUniversalApkFile() {
    val fakeCommandExecutor = FakeCommandExecutor()
    val bundleToolClient = BundleToolClient(
      tempFolder.root.absolutePath,
      scriptBgDispatcher,
      fakeCommandExecutor
    )

    val inputAabPath = "${tempFolder.root.absolutePath}/multi_config.aab"
    val outputApkPath = "${tempFolder.root.absolutePath}/universal.apk"

    val apksFile = tempFolder.newFile("universal.apk.apks")
    val zipOutputStream = ZipOutputStream(apksFile.outputStream())
    zipOutputStream.putNextEntry(ZipEntry("universal.apk"))
    zipOutputStream.write("universal apk with multiple configurations".toByteArray())
    zipOutputStream.closeEntry()
    zipOutputStream.close()

    setupFakeCommandExecutorForBuildUniversalApk(
      fakeCommandExecutor,
      inputAabPath,
      "$outputApkPath.apks"
    )

    val result = bundleToolClient.buildUniversalApk(
      inputAabPath,
      outputApkPath
    )

    assertThat(result.exists()).isTrue()
    assertThat(result.absolutePath).isEqualTo(outputApkPath)
    assertThat(result.readText()).isEqualTo("universal apk with multiple configurations")
  }

  private fun setupFakeCommandExecutorForBuildApks(
    fakeCommandExecutor: FakeCommandExecutor,
    inputBundlePath: String,
    outputApksPath: String
  ) {
    fakeCommandExecutor.registerHandler("java") { _, args, _, _ ->
      if (args.contains("build-apks") &&
        args.contains("--bundle=$inputBundlePath") &&
        args.contains("--output=$outputApksPath")
      ) {
        return@registerHandler 0
      }
      return@registerHandler 1
    }
  }

  private fun setupFakeCommandExecutorForBuildUniversalApk(
    fakeCommandExecutor: FakeCommandExecutor,
    inputBundlePath: String,
    outputApksPath: String
  ) {
    fakeCommandExecutor.registerHandler("java") { _, args, _, _ ->
      if (args.contains("build-apks") &&
        args.contains("--bundle=$inputBundlePath") &&
        args.contains("--output=$outputApksPath") &&
        args.contains("--mode=universal")
      ) {
        return@registerHandler 0
      }
      return@registerHandler 1
    }
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
