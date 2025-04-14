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
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Tests for [Aapt2Client].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class Aapt2ClientTest {
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
  fun testDumpPermissions_nonExistentApk_failsWithError() {
    val aapt2Client = createAapt2Client()
    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpPermissions("fake_file.apk")
    }
    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testDumpPermissions_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpPermissions(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("failed opening zip: Invalid file.")
  }

  @Test
  fun testDumpPermissions_apkWithNoPermissions_returnsEmptyList() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForPermissions(apkFile.absolutePath)
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val permissions = aapt2Client.dumpPermissions(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(permissions).isEmpty()
  }

  @Test
  fun testDumpPermissions_apkWithSomePermissions_returnsListOfQualifiedPermissionNames() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForPermissions(
      apkFile.absolutePath,
      "uses-permission: name='android.permission.INTERNET'",
      "uses-permission: name='android.permission.ACCESS_NETWORK_STATE'"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val permissions = aapt2Client.dumpPermissions(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(permissions).hasSize(2)
    assertThat(permissions).contains("uses-permission: name='android.permission.INTERNET'")
    assertThat(permissions)
      .contains("uses-permission: name='android.permission.ACCESS_NETWORK_STATE'")
  }

  @Test
  fun testDumpResources_nonExistentApk_failsWithError() {
    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpResources("fake_file.apk")
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testDumpResources_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpResources(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("failed opening zip: Invalid file.")
  }

  @Test
  fun testDumpResources_apkWithNoResources_returnsEmptyList() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForResources(apkFile.absolutePath)
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val resources = aapt2Client.dumpResources(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(resources).isEmpty()
  }

  @Test
  fun testDumpResources_apkWithOnlyStrings_returnsListWithResourcesWithTypesAndIds() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8),
        "resources.arsc" to ByteArray(100)
      )
    )

    setupFakeCommandForResources(
      apkFile.absolutePath,
      "resource 0x7f0b0000 string/app_name",
      "resource 0x7f0b0001 string/hello_world"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val resources = aapt2Client.dumpResources(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(resources).hasSize(2)
    assertThat(resources).contains("resource 0x7f0b0000 string/app_name")
    assertThat(resources).contains("resource 0x7f0b0001 string/hello_world")
  }

  @Test
  fun testDumpResources_apkWithOnlyDrawables_returnsListWithResourcesWithTypesAndIds() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8),
        "resources.arsc" to ByteArray(100)
      )
    )

    setupFakeCommandForResources(
      apkFile.absolutePath,
      "resource 0x7f070000 drawable/ic_launcher",
      "resource 0x7f070001 drawable/ic_background"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val resources = aapt2Client.dumpResources(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(resources).hasSize(2)
    assertThat(resources).contains("resource 0x7f070000 drawable/ic_launcher")
    assertThat(resources).contains("resource 0x7f070001 drawable/ic_background")
  }

  @Test
  fun testDumpResources_apkWithOnlyLayouts_returnsListWithResourcesWithTypesAndIds() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8),
        "resources.arsc" to ByteArray(100)
      )
    )

    setupFakeCommandForResources(
      apkFile.absolutePath,
      "resource 0x7f0c0000 layout/activity_main",
      "resource 0x7f0c0001 layout/fragment_home"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val resources = aapt2Client.dumpResources(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(resources).hasSize(2)
    assertThat(resources).contains("resource 0x7f0c0000 layout/activity_main")
    assertThat(resources).contains("resource 0x7f0c0001 layout/fragment_home")
  }

  @Test
  fun testDumpResources_apkWithManyResources_returnsListWithResourcesWithTypesAndIds() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8),
        "resources.arsc" to ByteArray(300)
      )
    )

    setupFakeCommandForResources(
      apkFile.absolutePath,
      "resource 0x7f0b0000 string/app_name",
      "resource 0x7f070000 drawable/ic_launcher",
      "resource 0x7f0c0000 layout/activity_main",
      "resource 0x7f010000 anim/fade_in",
      "resource 0x7f030000 color/primary_color"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val resources = aapt2Client.dumpResources(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(resources).hasSize(5)
    assertThat(resources).contains("resource 0x7f0b0000 string/app_name")
    assertThat(resources).contains("resource 0x7f070000 drawable/ic_launcher")
    assertThat(resources).contains("resource 0x7f0c0000 layout/activity_main")
    assertThat(resources).contains("resource 0x7f010000 anim/fade_in")
    assertThat(resources).contains("resource 0x7f030000 color/primary_color")
  }

  @Test
  fun testDumpBadging_nonExistentApk_failsWithError() {
    val aapt2Client = createAapt2Client()
    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpBadging("fake_file.apk")
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testDumpBadging_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")
    createdFiles.add(invalidApkFile)

    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpBadging(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("failed opening zip: Invalid file.")
  }

  @Test
  fun testDumpBadging_apkWithNoExtraBadgingInfo_returnsPackageAndGenericInfo() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForBadging(
      apkFile.absolutePath,
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val badging = aapt2Client.dumpBadging(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(badging).hasSize(3)
    assertThat(badging)
      .contains("package: name='org.oppia.android' versionCode='1' versionName='1.0'")
    assertThat(badging).contains("sdkVersion:'21'")
    assertThat(badging).contains("targetSdkVersion:'30'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesFeatures_returnsBadgingInfo() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForBadging(
      apkFile.absolutePath,
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'",
      "uses-feature: name='android.hardware.camera'",
      "uses-feature: name='android.hardware.bluetooth'"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val badging = aapt2Client.dumpBadging(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(badging).hasSize(5)
    assertThat(badging).contains("uses-feature: name='android.hardware.camera'")
    assertThat(badging).contains("uses-feature: name='android.hardware.bluetooth'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesImpliedFeatures_returnsBadgingInfo() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForBadging(
      apkFile.absolutePath,
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'",
      "uses-implied-feature: name='android.hardware.microphone'" +
        " reason='requested android.permission.RECORD_AUDIO permission'",
      "uses-implied-feature: name='android.hardware.location'" +
        " reason='requested android.permission.ACCESS_FINE_LOCATION permission'"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val badging = aapt2Client.dumpBadging(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(badging).hasSize(5)
    assertThat(badging).contains(
      "uses-implied-feature: name='android.hardware.microphone' " +
        "reason='requested android.permission.RECORD_AUDIO permission'"
    )
    assertThat(badging).contains(
      "uses-implied-feature: name='android.hardware.location' " +
        "reason='requested android.permission.ACCESS_FINE_LOCATION permission'"
    )
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesFeaturesNotRequired_returnsBadgingInfo() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForBadging(
      apkFile.absolutePath,
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'",
      "uses-feature-not-required: name='android.hardware.camera.front'",
      "uses-feature-not-required: name='android.hardware.nfc'"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val badging = aapt2Client.dumpBadging(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(badging).hasSize(5)
    assertThat(badging).contains("uses-feature-not-required: name='android.hardware.camera.front'")
    assertThat(badging).contains("uses-feature-not-required: name='android.hardware.nfc'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesPermission_returnsBadgingInfo() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    setupFakeCommandForBadging(
      apkFile.absolutePath,
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'",
      "uses-permission: name='android.permission.INTERNET'",
      "uses-permission: name='android.permission.ACCESS_NETWORK_STATE'"
    )
    val aapt2Client = createAapt2ClientWithFakeExecutor()

    val badging = aapt2Client.dumpBadging(apkFile.absolutePath).filter { it.isNotBlank() }

    assertThat(badging).hasSize(5)
    assertThat(badging).contains("uses-permission: name='android.permission.INTERNET'")
    assertThat(badging).contains("uses-permission: name='android.permission.ACCESS_NETWORK_STATE'")
  }

  @Test
  fun testDumpBadging_apkWithAllOppiaLikeBadgingInfo_returnsBadgingInfo() {
    val apkFile = createValidApkFile(
      "test.apk",
      mapOf(
        "classes.dex" to ByteArray(100),
        "AndroidManifest.xml" to "<manifest></manifest>".toByteArray(StandardCharsets.UTF_8)
      )
    )

    val expectedBadgingOutput = listOf(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'",
      "uses-permission: name='android.permission.INTERNET'",
      "uses-permission: name='android.permission.ACCESS_NETWORK_STATE'",
      "uses-feature: name='android.hardware.camera'",
      "uses-feature-not-required: name='android.hardware.nfc'",
      "uses-implied-feature: name='android.hardware.microphone' " +
        "reason='requested android.permission.RECORD_AUDIO permission'",
      "application-label:'Oppia'",
      "application-label-fr:'Oppia'",
      "application-label-es:'Oppia'",
      "application-icon-160:'res/drawable/ic_launcher.png'",
      "application-icon-240:'res/drawable-hdpi/ic_launcher.png'",
      "application-icon-320:'res/drawable-xhdpi/ic_launcher.png'",
      "application: label='Oppia' icon='res/drawable/ic_launcher.png'",
      "launchable-activity: " +
        "name='org.oppia.android.app.activity.SplashActivity'  label='Oppia' icon=''",
      "densities: 160 240 320"
    )

    setupFakeCommandForBadging(apkFile.absolutePath, *expectedBadgingOutput.toTypedArray())

    val aapt2Client = createAapt2ClientWithFakeExecutor()
    val actualBadgingOutput = aapt2Client.dumpBadging(apkFile.absolutePath)
      .filter { it.isNotBlank() }

    assertThat(actualBadgingOutput).hasSize(expectedBadgingOutput.size)
    assertThat(actualBadgingOutput).containsExactlyElementsIn(expectedBadgingOutput)
  }

  private fun createAapt2Client(): Aapt2Client {
    return Aapt2Client(
      tempFolder.root.absolutePath,
      sdkProperties.buildToolsVersion,
      scriptBgDispatcher,
      commandExecutor
    )
  }

  private fun createAapt2ClientWithFakeExecutor(): Aapt2Client {
    return Aapt2Client(
      tempFolder.root.absolutePath,
      sdkProperties.buildToolsVersion,
      scriptBgDispatcher,
      fakeCommandExecutor
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

  private fun setupFakeCommand(apkPath: String, dumpType: String, vararg outputLines: String) {
    val aapt2Path = File(
      "external/androidsdk", "build-tools/${sdkProperties.buildToolsVersion}/aapt2"
    ).absolutePath

    fakeCommandExecutor.registerHandler(aapt2Path) { _, args, outputStream, _ ->
      if (args.size >= 3 && args[0] == "dump" && args[1] == dumpType && args[2] == apkPath) {
        outputLines.forEach { outputStream.println(it) }
        return@registerHandler 0
      }
      return@registerHandler 1
    }
  }

  private fun setupFakeCommandForPermissions(apkPath: String, vararg permissionInfo: String) {
    setupFakeCommand(apkPath, "permissions", *permissionInfo)
  }

  private fun setupFakeCommandForResources(apkPath: String, vararg resourceInfo: String) {
    setupFakeCommand(apkPath, "resources", *resourceInfo)
  }

  private fun setupFakeCommandForBadging(apkPath: String, vararg badgingInfo: String) {
    setupFakeCommand(apkPath, "badging", *badgingInfo)
  }
}
