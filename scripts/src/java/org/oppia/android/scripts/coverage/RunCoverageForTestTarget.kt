package org.oppia.android.scripts.coverage

import kotlinx.coroutines.runBlocking
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File

/**
 * Entry point function for running coverage analysis for a single test target.
 *
 * Usage:
 *   bazel run //scripts:run_coverage_for_test_target -- <path_to_root> <//:test_targetname>
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - test_targetname: bazel target name of the test
 *
 * Example:
 *     bazel run //scripts:run_coverage_for_test_target -- $(pwd)
 *     //utility/src/test/java/org/oppia/android/util/parser/math:MathModelTest
 */
fun main(vararg args: String) {
  val repoRoot = File(args[0]).absoluteFile.normalize()
  val targetPath = args[1]

  RunCoverageForTestTarget(repoRoot, targetPath).runCoverage()
}

/**
 * Class responsible for analyzing target files for coverage and generating reports.
 */
class RunCoverageForTestTarget(
  private val repoRoot: File,
  private val targetPath: String
) {

  /**
   * Analyzes target file for coverage, generates chosen reports accordingly.
   */
  fun runCoverage(): String? {
    return runWithCoverageAnalysis()
  }

  /**
   * Runs coverage analysis on the specified target file asynchronously.
   *
   * @return [Path of the coverage data file].
   */
  fun runWithCoverageAnalysis(): String? {
    return ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
      runBlocking {
        val result =
          CoverageRunner(repoRoot, scriptBgDispatcher).runWithCoverageAsync(targetPath).await()
        result
      }
    }
  }
}
