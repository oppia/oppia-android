package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.testing.assertThrows
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

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

  // TODO(#4971): Finish the tests for this suite.

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testComputeDownloadSize_forNonExistentApk_throwsException() {
    val apkAnalyzerClient = createApkAnalyzerClient()

    val exception = assertThrows<IllegalArgumentException>() {
      apkAnalyzerClient.computeDownloadSize("fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("Cannot open apk")
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

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
