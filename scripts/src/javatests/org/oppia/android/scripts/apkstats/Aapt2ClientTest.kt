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

  // TODO(#4971): Finish the tests for this suite.

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testDumpResources_forNonExistentApk_throwsException() {
    val aapt2Client = createAapt2Client()

    val exception = assertThrows<IllegalStateException>() {
      aapt2Client.dumpResources("fake_file.apk")
    }

    assertThat(exception).hasMessageThat().contains("No such file or directory")
  }

  private fun createAapt2Client(): Aapt2Client {
    return Aapt2Client(
      tempFolder.root.absolutePath,
      sdkProperties.buildToolsVersion,
      scriptBgDispatcher,
      commandExecutor
    )
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
