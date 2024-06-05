package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.coverage.CoverageRunner
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Entry point function for running coverage analysis for a single test target.
 *
 * @param args Command-line arguments.
 */
fun main(vararg args: String) {
  val repoRoot = File(args[0]).absoluteFile.normalize()
  val targetPath = args[1]

  RunCoverageForTestTarget().runCoverage(repoRoot, targetPath)
}

/**
 * Class responsible for analyzing target files for coverage and generating reports.
 */
class RunCoverageForTestTarget() {

  /**
   * Analyzes target file for coverage, generates chosen reports accordingly.
   *
   * @param repoRoot the absolute path to the working root directory
   * @param targetFile Path to the file to analyze.
   */
  fun runCoverage(repoRoot: File, targetPath: String) {
    runWithCoverageAnalysis(repoRoot, targetPath)
  }

  /**
   * Runs coverage analysis on the specified target file asynchronously.
   *
   * @param repoRoot the absolute path to the working root directory
   * @param targetFile Path to the target file to analyze coverage.
   * @return [A deferred result representing the coverage report].
   */
  fun runWithCoverageAnalysis(repoRoot: File, targetPath: String) {
    ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
      runBlocking {
        CoverageRunner().runWithCoverageAsync(repoRoot, scriptBgDispatcher, targetPath).await()
      }
    }
  }
}
