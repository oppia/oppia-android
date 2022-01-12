package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.testing.assertThrows

/**
 * Tests for [BundleToolClient].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class BundleToolClientTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  // TODO: finish tests for this suite.

  @Test
  fun testBuildUniversalApk_forNonExistentAab_throwsException() {
    val bundleToolClient = BundleToolClient(tempFolder.root.absolutePath, commandExecutor)

    val exception = assertThrows(IllegalStateException::class) {
      bundleToolClient.buildUniversalApk("fake.aab", "fake.apk")
    }

    assertThat(exception).hasMessageThat().contains("was not found")
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES)
  }
}
