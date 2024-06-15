package org.oppia.android.scripts.coverage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Class responsible for running coverage analysis asynchronously.
 *
 * @param repoRoot the absolute path to the working root directory
 * @param targetFile Path to the target file to analyze coverage.
 */
class CoverageRunner(
  private val repoRoot: File,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {

  /**
   * Runs coverage analysis asynchronously for the Bazel test target.
   *
   * @param repoRoot the absolute path to the working root directory
   * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
   * @param bazelTestTarget Bazel test target to analyze coverage.
   * @return a deferred value that contains the path of the coverage data file [will contain the proto for the coverage data].
   */
  suspend fun runWithCoverageAsync(
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
   * @param repoRoot the absolute path to the working root directory
   * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
   * @param bazelTestTarget Bazel test target to analyze coverage.
   * @return a lisf of string that contains the result of the coverage execution.
   */
  fun getCoverage(
    bazelTestTarget: String
  ): String? {
    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    val coverageDataBinary = bazelClient.runCoverageForTestTarget(bazelTestTarget)
    val coverageDataString = convertByteArrayToString(coverageDataBinary!!)

    return coverageDataString
  }

  /**
   * Converts a ByteArray to a String using UTF-8 encoding.
   *
   * @param bytes byte array to convert
   * @return string representation of the byte array
   */
  fun convertByteArrayToString(coverageBinaryData: ByteArray?): String? {
    return String(coverageBinaryData!!, Charsets.UTF_8)
  }
}
