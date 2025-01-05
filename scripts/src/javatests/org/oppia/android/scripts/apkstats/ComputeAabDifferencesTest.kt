package org.oppia.android.scripts.apkstats

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.testing.assertThrows
import java.lang.IllegalStateException

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

  private val briefSummaryOutputPath = "path/to/brief/summary/output/file.txt"
  private val fullSummaryOutputPath = "path/to/full/summary/output/file.txt"
  private val buildFlavor = "dev"
  private val oldAabPath = "path/to/old/aab/file.aab"
  private val newAabPath = "path/to/new/aab/file.aab"

  // TODO(#4971): Finish the tests for this suite.

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
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
      main(briefSummaryOutputPath, fullSummaryOutputPath)
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_threeArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(briefSummaryOutputPath, fullSummaryOutputPath, buildFlavor)
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  @Test
  fun testMain_fourArguments_failsWithError() {
    val exception = assertThrows<IllegalStateException> {
      main(briefSummaryOutputPath, fullSummaryOutputPath, buildFlavor, oldAabPath)
    }

    assertThat(exception).hasMessageThat().contains("Expected at least 1 triplet entry")
  }

  private fun createComputeAabDifferences(): ComputeAabDifferences {
    return ComputeAabDifferences(
      workingDirectoryPath = tempFolder.root.absoluteFile.normalize().path,
      sdkProperties = AndroidBuildSdkProperties(),
      scriptBgDispatcher
    )
  }

  private fun createProfile(
    oldAabFilePath: String,
    newAabFilePath: String,
    buildFlavor: String = "dev"
  ): ComputeAabDifferences.AabProfile {
    return ComputeAabDifferences.AabProfile(buildFlavor, oldAabFilePath, newAabFilePath)
  }
}
