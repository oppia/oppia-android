package org.oppia.android.scripts.lint

import java.io.File
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher

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

class Buildifier(private val repoRoot: File, private val bazelClient: BazelClient) {
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

  enum class Mode(val lintModeStr: String, val buildifierModeStr: String) {
    CHECK(lintModeStr = "warn", buildifierModeStr = "check"),
    FIX(lintModeStr = "fix", buildifierModeStr = "fix")
  }

  private companion object {
    private val EXTRA_ENABLED_WARNINGS = listOf("out-of-order-load", "unsorted-dict-items")
    private val EXTRA_DISABLED_WARNINGS = listOf("native-android")
    private const val BUILDIFIER_BINARY_TARGET = "@com_github_bazelbuild_buildtools//buildifier"
  }
}
