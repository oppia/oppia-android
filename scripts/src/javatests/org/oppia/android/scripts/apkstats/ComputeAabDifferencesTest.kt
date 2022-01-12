package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.testing.assertThrows
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

/**
 * Tests for [ComputeAabDifferences].
 *
 * Note that this test executes real commands on the local filesystem.
 */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class ComputeAabDifferencesTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private val sdkProperties = AndroidBuildSdkProperties()

  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  // TODO: finish tests for this suite.

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

    val exception = assertThrows(IllegalStateException::class) {
      differencesUtility.computeBuildStats(profile)
    }

    assertThat(exception).hasMessageThat().contains("was not found")
  }

  private fun createComputeAabDifferences(): ComputeAabDifferences {
    val aapt2Client = Aapt2Client(
      tempFolder.root.absolutePath,
      sdkProperties.buildToolsVersion,
      commandExecutor
    )
    return ComputeAabDifferences(
      aapt2Client,
      ApkAnalyzerClient(aapt2Client),
      BundleToolClient(tempFolder.root.absolutePath, commandExecutor)
    )
  }

  private fun createProfile(
    oldAabFilePath: String,
    newAabFilePath: String,
    buildFlavor: String = "dev"
  ): ComputeAabDifferences.AabProfile {
    return ComputeAabDifferences.AabProfile(buildFlavor, oldAabFilePath, newAabFilePath)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES)
  }
}
