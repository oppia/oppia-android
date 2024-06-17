package org.oppia.android.scripts.coverage

import kotlinx.coroutines.runBlocking
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.util.concurrent.TimeUnit

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
 * Example with custom process timeout:
 *     bazel run //scripts:run_coverage_for_test_target -- $(pwd)
 *     //utility/src/test/java/org/oppia/android/util/parser/math:MathModelTest processTimeout=10
 *
 */
fun main(vararg args: String) {
  val repoRoot = File(args[0]).absoluteFile.normalize()
  val targetPath = args[1]

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    RunCoverageForTestTarget(
      repoRoot,
      targetPath,
      commandExecutor,
      scriptBgDispatcher
    ).runCoverage()
  }
}

/**
 * Class responsible for analyzing target files for coverage and generating reports.
 *
 * @param repoRoot the root directory of the repository
 * @param targetPath Bazel test target to analyze coverage
 * @param commandExecutor Executes the specified command in the specified working directory
 */
class RunCoverageForTestTarget(
  private val repoRoot: File,
  private val targetPath: String,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
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
   * @return the generated coverage data.
   */
  fun runWithCoverageAnalysis(): String? {
    return runBlocking {
      val result =
        CoverageRunner(repoRoot, scriptBgDispatcher, commandExecutor)
          .runWithCoverageAsync(targetPath)
          .await()
      result
    }
  }
}
