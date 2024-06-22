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
 * @param commandExecutor executes the specified command in the specified working directory
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
   * @param bazelTestTarget Bazel test target to analyze coverage
   * @return a deferred value that contains the coverage data
   */
  fun runWithCoverageAsync(
    bazelTestTarget: String
  ): Deferred<List<String>?> {
    return CoroutineScope(scriptBgDispatcher).async {
      retrieveCoverageResult(bazelTestTarget)
    }
  }

  private fun retrieveCoverageResult(
    bazelTestTarget: String
  ): List<String>? {
    return bazelClient.runCoverageForTestTarget(bazelTestTarget)
  }
}
