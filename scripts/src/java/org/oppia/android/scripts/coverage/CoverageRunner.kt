package org.oppia.android.scripts.coverage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File

/**
 * Class responsible for running coverage analysis asynchronously.
 *
 * @param repoRoot the root directory of the repository
 * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
 * @param commandExecutor Executes the specified command in the specified working directory
 */
class CoverageRunner(
  private val repoRoot: File,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val commandExecutor: CommandExecutor
) {
  private val bazelClient by lazy { BazelClient(repoRoot, commandExecutor) }

  /**
   * Runs coverage analysis asynchronously for the Bazel test target.
   *
   * @param bazelTestTarget Bazel test target to analyze coverage.
   * @return a deferred value that contains the coverage data [will contain the proto for the coverage data].
   */
  fun runWithCoverageAsync(
    bazelTestTarget: String
  ): Deferred<String?> {
    return CoroutineScope(scriptBgDispatcher).async {
      val coverageDataFilePath = getCoverage(bazelTestTarget)
      coverageDataFilePath
    }
  }

  /**
   * Runs coverage command for the Bazel test target.
   *
   * @param bazelTestTarget Bazel test target to analyze coverage.
   * @return the generated coverage data as a string.
   */
  fun getCoverage(
    bazelTestTarget: String
  ): String? {
    val coverageDataBinary = bazelClient.runCoverageForTestTarget(bazelTestTarget)
    val coverageDataString = convertByteArrayToString(coverageDataBinary!!)

    return coverageDataString
  }

  /**
   * Converts a ByteArray to a String using UTF-8 encoding.
   *
   * @param coverageBinaryData byte array to convert
   * @return string representation of the byte array
   */
  fun convertByteArrayToString(coverageBinaryData: ByteArray?): String? {
    return String(coverageBinaryData!!, Charsets.UTF_8)
  }
}
