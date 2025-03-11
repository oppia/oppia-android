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
import java.util.concurrent.TimeUnit

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

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testDumpPermissions_nonExistentApk_failsWithError() {
    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException> {
      aapt2Client.dumpPermissions("fake_file.apk")
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testDumpPermissions_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")

    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpPermissions(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("failed opening zip: Invalid file.")
  }

  @Test
  fun testDumpPermissions_apkWithNoPermissions_returnsEmptyList() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForNoPermissions()

    val permissions = aapt2Client.dumpPermissions("test.apk").filter { it.isNotBlank() }

    assertThat(permissions).isEmpty()
  }

  @Test
  fun testDumpPermissions_apkWithSomePermissions_returnsListOfQualifiedPermissionNames() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForPermissions(
      "uses-permission: name='android.permission.INTERNET'",
      "uses-permission: name='android.permission.ACCESS_NETWORK_STATE'",
      "uses-permission: name='android.permission.CAMERA'"
    )

    val permissions = aapt2Client.dumpPermissions("test.apk").filter { it.isNotBlank() }

    assertThat(permissions).hasSize(3)
    assertThat(permissions).contains("uses-permission: name='android.permission.INTERNET'")
    assertThat(permissions)
      .contains("uses-permission: name='android.permission.ACCESS_NETWORK_STATE'")
    assertThat(permissions).contains("uses-permission: name='android.permission.CAMERA'")
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

    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpResources(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("failed opening zip: Invalid file.")
  }

  @Test
  fun testDumpResources_apkWithNoResources_returnsEmptyList() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForNoResources()

    val resources = aapt2Client.dumpResources("test.apk").filter { it.isNotBlank() }

    assertThat(resources).isEmpty()
  }

  @Test
  fun testDumpResources_apkWithOnlyStrings_returnsListWithResourcesWithTypesAndIds() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForResources(
      "resource 0x7f030000 string/app_name: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f030001 string/welcome_message: t=0x03 d=0x00000005 (s=0x0008 r=0x00)"
    )

    val resources = aapt2Client.dumpResources("test.apk").filter { it.isNotBlank() }

    assertThat(resources).hasSize(2)
    assertThat(resources[0]).contains("resource 0x7f030000 string/app_name")
    assertThat(resources[1]).contains("resource 0x7f030001 string/welcome_message")
  }

  @Test
  fun testDumpResources_apkWithOnlyDrawables_returnsListWithResourcesWithTypesAndIds() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForResources(
      "resource 0x7f040000 drawable/ic_launcher: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f040001 drawable/background: t=0x03 d=0x00000005 (s=0x0008 r=0x00)"
    )

    val resources = aapt2Client.dumpResources("test.apk").filter { it.isNotBlank() }

    assertThat(resources).hasSize(2)
    assertThat(resources[0]).contains("resource 0x7f040000 drawable/ic_launcher")
    assertThat(resources[1]).contains("resource 0x7f040001 drawable/background")
  }

  @Test
  fun testDumpResources_apkWithOnlyLayouts_returnsListWithResourcesWithTypesAndIds() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForResources(
      "resource 0x7f050000 layout/activity_main: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f050001 layout/fragment_detail: t=0x03 d=0x00000005 (s=0x0008 r=0x00)"
    )

    val resources = aapt2Client.dumpResources("test.apk").filter { it.isNotBlank() }

    assertThat(resources).hasSize(2)
    assertThat(resources[0]).contains("resource 0x7f050000 layout/activity_main")
    assertThat(resources[1]).contains("resource 0x7f050001 layout/fragment_detail")
  }

  @Test
  fun testDumpResources_apkWithManyResources_returnsListWithResourcesWithTypesAndIds() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForResources(
      "resource 0x7f030000 string/app_name: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f040000 drawable/ic_launcher: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f050000 layout/activity_main: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f060000 color/primary: t=0x03 d=0x00000004 (s=0x0008 r=0x00)",
      "resource 0x7f070000 dimen/margin_small: t=0x03 d=0x00000004 (s=0x0008 r=0x00)"
    )

    val resources = aapt2Client.dumpResources("test.apk").filter { it.isNotBlank() }

    assertThat(resources).hasSize(5)
    assertThat(resources[0]).contains("resource 0x7f030000 string/app_name")
    assertThat(resources[1]).contains("resource 0x7f040000 drawable/ic_launcher")
    assertThat(resources[2]).contains("resource 0x7f050000 layout/activity_main")
    assertThat(resources[3]).contains("resource 0x7f060000 color/primary")
    assertThat(resources[4]).contains("resource 0x7f070000 dimen/margin_small")
  }

  @Test
  fun testDumpBadging_nonExistentApk_failsWithError() {
    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException> {
      aapt2Client.dumpBadging("fake_file.apk")
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  @Test
  fun testDumpBadging_invalidApk_failsWithError() {
    val invalidApkFile = File(tempFolder.root, "invalid.apk")
    invalidApkFile.writeText("This is not a valid APK file")

    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpBadging(invalidApkFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("failed opening zip: Invalid file.")
  }

  @Test
  fun testDumpBadging_apkWithNoExtraBadgingInfo_returnsPackageAndGenericInfo() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForBadging(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'"
    )

    val badging = aapt2Client.dumpBadging("test.apk").filter { it.isNotBlank() }

    assertThat(badging).hasSize(3)
    assertThat(badging[0]).contains("package: name='org.oppia.android'")
    assertThat(badging[1]).contains("sdkVersion:'21'")
    assertThat(badging[2]).contains("targetSdkVersion:'30'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesFeatures_returnsBadgingInfo() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForBadging(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "uses-feature: name='android.hardware.camera'",
      "uses-feature: name='android.hardware.camera.autofocus'"
    )

    val badging = aapt2Client.dumpBadging("test.apk").filter { it.isNotBlank() }

    assertThat(badging).hasSize(3)
    assertThat(badging[0]).contains("package: name='org.oppia.android'")
    assertThat(badging[1]).contains("uses-feature: name='android.hardware.camera'")
    assertThat(badging[2]).contains("uses-feature: name='android.hardware.camera.autofocus'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesImpliedFeatures_returnsBadgingInfo() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForBadging(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "uses-implied-feature: name='android.hardware.camera'" +
        " reason='requested android.permission.CAMERA permission'",
      "uses-implied-feature: name='android.hardware.location'" +
        " reason='requested android.permission.ACCESS_FINE_LOCATION permission'"
    )

    val badging = aapt2Client.dumpBadging("test.apk").filter { it.isNotBlank() }

    assertThat(badging).hasSize(3)
    assertThat(badging[0]).contains("package: name='org.oppia.android'")
    assertThat(badging[1]).contains("uses-implied-feature: name='android.hardware.camera'")
    assertThat(badging[2]).contains("uses-implied-feature: name='android.hardware.location'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesFeaturesNotRequired_returnsBadgingInfo() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForBadging(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "uses-feature-not-required: name='android.hardware.camera'",
      "uses-feature-not-required: name='android.hardware.bluetooth'"
    )

    val badging = aapt2Client.dumpBadging("test.apk").filter { it.isNotBlank() }

    assertThat(badging).hasSize(3)
    assertThat(badging[0]).contains("package: name='org.oppia.android'")
    assertThat(badging[1]).contains("uses-feature-not-required: name='android.hardware.camera'")
    assertThat(badging[2]).contains("uses-feature-not-required: name='android.hardware.bluetooth'")
  }

  @Test
  fun testDumpBadging_apkWithOnlyUsesPermission_returnsBadgingInfo() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForBadging(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "uses-permission: name='android.permission.INTERNET'",
      "uses-permission: name='android.permission.ACCESS_NETWORK_STATE'"
    )

    val badging = aapt2Client.dumpBadging("test.apk").filter { it.isNotBlank() }

    assertThat(badging).hasSize(3)
    assertThat(badging[0]).contains("package: name='org.oppia.android'")
    assertThat(badging[1]).contains("uses-permission: name='android.permission.INTERNET'")
    assertThat(badging[2])
      .contains("uses-permission: name='android.permission.ACCESS_NETWORK_STATE'")
  }

  @Test
  fun testDumpBadging_apkWithAllOppiaLikeBadgingInfo_returnsBadgingInfo() {
    val aapt2Client = createAapt2ClientWithFakeExecutor()
    setupFakeCommandExecutorForBadging(
      "package: name='org.oppia.android' versionCode='1' versionName='1.0'",
      "sdkVersion:'21'",
      "targetSdkVersion:'30'",
      "uses-permission: name='android.permission.INTERNET'",
      "uses-permission: name='android.permission.ACCESS_NETWORK_STATE'",
      "uses-feature: name='android.hardware.camera'",
      "uses-feature-not-required: name='android.hardware.bluetooth'",
      "uses-implied-feature: name='android.hardware.location'" +
        " reason='requested android.permission.ACCESS_FINE_LOCATION permission'",
      "supports-screens: 'small' 'normal' 'large' 'xlarge'",
      "supports-densities: '160' '240' '320' '480' '640'",
    )

    val badging = aapt2Client.dumpBadging("test.apk").filter { it.isNotBlank() }

    assertThat(badging).hasSize(10)

    assertThat(badging[0]).contains("package: name='org.oppia.android'")
    assertThat(badging[1]).contains("sdkVersion:'21'")
    assertThat(badging[2]).contains("targetSdkVersion:'30'")
    assertThat(badging[3]).contains("uses-permission: name='android.permission.INTERNET'")
    assertThat(badging[4])
      .contains("uses-permission: name='android.permission.ACCESS_NETWORK_STATE'")
    assertThat(badging[5]).contains("uses-feature: name='android.hardware.camera'")
    assertThat(badging[6]).contains("uses-feature-not-required: name='android.hardware.bluetooth'")
    assertThat(badging[7]).contains("uses-implied-feature: name='android.hardware.location'")
    assertThat(badging[8]).contains("supports-screens: 'small' 'normal' 'large' 'xlarge'")
    assertThat(badging[9]).contains("supports-densities: '160' '240' '320' '480' '640'")
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

  private fun setupFakeCommandExecutorForNoPermissions() {
    setupFakeCommandExecutorForCommand("dump", "permissions", emptyList())
  }

  private fun setupFakeCommandExecutorForPermissions(vararg permissions: String) {
    setupFakeCommandExecutorForCommand("dump", "permissions", permissions.toList())
  }

  private fun setupFakeCommandExecutorForNoResources() {
    setupFakeCommandExecutorForCommand("dump", "resources", emptyList())
  }

  private fun setupFakeCommandExecutorForResources(vararg resources: String) {
    setupFakeCommandExecutorForCommand("dump", "resources", resources.toList())
  }

  private fun setupFakeCommandExecutorForBadging(vararg badgingInfo: String) {
    setupFakeCommandExecutorForCommand("dump", "badging", badgingInfo.toList())
  }

  private fun setupFakeCommandExecutorForCommand(
    command: String,
    subCommand: String,
    output: List<String>
  ) {
    val aapt2Path = File(
      "external/androidsdk", "build-tools/${sdkProperties.buildToolsVersion}/aapt2"
    ).absolutePath

    fakeCommandExecutor.registerHandler(aapt2Path) { _, args, outputStream, _ ->
      if (args.size >= 2 && args[0] == command && args[1] == subCommand) {
        output.forEach { outputStream.println(it) }
        return@registerHandler 0
      }
      return@registerHandler 1
    }
  }
}
