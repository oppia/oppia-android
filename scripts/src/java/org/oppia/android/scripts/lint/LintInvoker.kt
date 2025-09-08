package org.oppia.android.scripts.lint

import kotlin.system.exitProcess
import com.android.tools.lint.Main as LintCli

/**
 * Standalone wrapper for running Android Lint that ensures proper process termination.
 *
 * This class is designed to be run as a separate Bazel target to avoid timeout and stalling
 * issues that can occur when running Lint directly within the main script process.
 *
 * Usage via Bazel:
 *   bazel run //scripts:lint_invoker -- [lint-args]
 */
object LintInvoker {
  @JvmStatic
  fun main(args: Array<String>) {
    try {

      val exitCode = LintCli().run(args)

      exitProcess(exitCode)
    } catch (e: Exception) {
      exitProcess(1)
    }
  }
}
