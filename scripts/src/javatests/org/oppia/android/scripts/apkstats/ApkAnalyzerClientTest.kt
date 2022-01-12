package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.testing.assertThrows

/**
 * Tests for [ApkAnalyzerClient].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class ApkAnalyzerClientTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private val sdkProperties = AndroidBuildSdkProperties()

  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  // TODO: finish tests for this suite.

  @Test
  fun testComputeDownloadSize_forNonExistentApk_throwsException() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows(IllegalArgumentException::class) {
      apkAnalyzerClient.computeDownloadSize("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
  }

  private fun createApkAnalyzerClient(): ApkAnalyzerClient {
    return ApkAnalyzerClient(
      Aapt2Client(
        tempFolder.root.absolutePath,
        sdkProperties.buildToolsVersion,
        commandExecutor
      )
    )
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES)
  }
}
