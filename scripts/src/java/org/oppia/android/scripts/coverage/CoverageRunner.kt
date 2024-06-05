package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import java.io.File

/**
 * Class responsible for running coverage analysis asynchronously.
 */
class CoverageRunner {

  /**
   * Runs coverage analysis asynchronously for the Bazel test target.
   *
   * @param repoRoot the absolute path to the working root directory
   * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
   * @param bazelTestTarget Bazel test target to analyze coverage.
   * @return a deferred value that contains the path of the coverage data file [will contain the proto for the coverage data].
   */
  suspend fun runWithCoverageAsync(
    repoRoot: File,
    scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
    bazelTestTarget: String
  ): Deferred<String?> {
    return CoroutineScope(scriptBgDispatcher).async {
      val coverageData = getCoverage(repoRoot, scriptBgDispatcher, bazelTestTarget)
      val data = coverageData.await()
      parseCoverageDataFile(data)
    }
  }

  /**
   * Runs coverage command for the Bazel test target.
   *
   * @param repoRoot the absolute path to the working root directory
   * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
   * @param bazelTestTarget Bazel test target to analyze coverage.
   * @return a deferred value that contains the result of the coverage execution.
   */
  fun getCoverage(
    repoRoot: File,
    scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
    bazelTestTarget: String
  ): Deferred<List<String>> {
    return CoroutineScope(scriptBgDispatcher).async {
      val commandExecutor: CommandExecutor = CommandExecutorImpl(scriptBgDispatcher)
      val bazelClient = BazelClient(repoRoot, commandExecutor)
      val coverageData = bazelClient.runCoverageForTestTarget(bazelTestTarget)
      coverageData
    }
  }

  /**
   * Parse the coverage command result to extract the path of the coverage data file.
   *
   * @param data the result from the execution of the coverage command
   * @return the extracted path of the coverage data file.
   */
  fun parseCoverageDataFile(data: List<String>) : String? {
    val regex = ".*coverage\\.dat$".toRegex()
    for (line in data) {
      val match = regex.find(line)
      val extractedPath = match?.value?.substringAfterLast(",")?.trim()
      if (extractedPath != null) {
        println("Parsed Coverage Data File: $extractedPath")
        return extractedPath
      }
    }
    return null
  }
}
