package org.oppia.android.scripts.lint

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File

/**
 * The main entrypoint for running Bazel lint checks.
 *
 * This script wraps the Buildifier (https://github.com/bazelbuild/buildtools) utility for
 * performing lint checks on all BUILD, bzl, and WORKSPACE files in the repository. The script also
 * supports auto-fixing most failures.
 *
 * Usage:
 *   bazel run //scripts:buildifier -- <path_to_repo_root> <mode>
 *
 * Arguments:
 * - path_to_repo_root: directory path to the root of the Oppia Android repository.
 * - mode: specific mode to run the check in. One of: 'check' (to just check for failures) or 'fix'
 *     (to auto-fix found issues).
 *
 * Example:
 *   bazel run //scripts:buildifier -- $(pwd) fix
 */
fun main(vararg args: String) {
  require(args.size == 2) { "Usage: bazel run //scripts:buildifier -- </path/to/repo_root> <mode>" }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) {
      "Expected provided repository root to be an existing directory: ${args[0]}."
    }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    val buildifierRunner = Buildifier(repoRoot, bazelClient)
    val mode = when (args[1]) {
      "check" -> Buildifier.Mode.CHECK
      "fix" -> Buildifier.Mode.FIX
      else -> error("Error: unknown mode '${args[2]}'. Expected one of: check/fix.")
    }
    buildifierRunner.runBuildifier(mode)
  }
}

/**
 * Utility for running the Buildifier utility as part of verifying all BUILD, .bazel, .bzl, and
 * WORKSPACE files under [repoRoot].
 *
 * @property repoRoot the absolute [File] corresponding to the root of the inspected repository
 * @property bazelClient a [BazelClient] configured for a single repository at [repoRoot]
 */
class Buildifier(private val repoRoot: File, private val bazelClient: BazelClient) {
  /**
   * Performs a lint check on all Bazel files in the repository, throwing an exception if any have
   * lint failures.
   *
   * @param mode the specific [Mode] to run this check in (e.g. whether to auto-fix found issues)
   */
  fun runBuildifier(mode: Mode) {
    val hasNoBuildifierFailures = when (mode) {
      Mode.CHECK -> tryRunBuildifier(mode)
      Mode.FIX -> tryRunBuildifier(mode = Mode.CHECK, printOutput = false)
    }
    if (!hasNoBuildifierFailures) {
      when (mode) {
        Mode.CHECK -> {
          println()
          error("Buildifier command failed. Re-run with 'fix' in order to auto-fix issues.")
        }
        Mode.FIX -> {
          // There are failures, try to fix them.
          if (tryRunBuildifier(mode)) {
            println("Checking if autofix addressed everything...")
            println()
            if (!tryRunBuildifier(mode = Mode.CHECK)) {
              println()
              error("Failed to autofix all issues. Please fix them manually.")
            } else println("All issues were successfully auto-fixed!")
          } else error("Autofix command itself unexpectedly failed--try re-running with check.")
        }
      }
    } else {
      when (mode) {
        Mode.CHECK -> println("Buildifier command succeeded--no issues found!")
        Mode.FIX -> println("Skipping fix--there are no failures.")
      }
    }
  }

  private fun tryRunBuildifier(mode: Mode, printOutput: Boolean = true): Boolean {
    val warnings = EXTRA_ENABLED_WARNINGS.map { "+$it" } + EXTRA_DISABLED_WARNINGS.map { "-$it" }
    val warningLine = warnings.joinToString(separator = ",")
    val (exitCode, outputLines) = bazelClient.run(
      BUILDIFIER_BINARY_TARGET,
      "--lint=${mode.lintModeStr}",
      "--mode=${mode.buildifierModeStr}",
      "--warnings=$warningLine",
      "-r",
      repoRoot.path,
      allowFailures = true
    )
    if (printOutput) outputLines.forEach(::println)
    return exitCode == 0
  }

  /**
   * Modes that [Buildifier] can run in.
   *
   * @property lintModeStr the Buildifier lint mode passed to the binary
   * @property buildifierModeStr the Buildifier runtime mode passed to the binary
   */
  enum class Mode(val lintModeStr: String, val buildifierModeStr: String) {
    /** Represents checking, but not fixing, files for lint issues. */
    CHECK(lintModeStr = "warn", buildifierModeStr = "check"),

    /** Represents checking and attempting to auto-fix lint issues in files. */
    FIX(lintModeStr = "fix", buildifierModeStr = "fix")
  }

  private companion object {
    private val EXTRA_ENABLED_WARNINGS = listOf("out-of-order-load", "unsorted-dict-items")
    private val EXTRA_DISABLED_WARNINGS = listOf("native-android")
    private const val BUILDIFIER_BINARY_TARGET = "@com_github_bazelbuild_buildtools//buildifier"
  }
}
