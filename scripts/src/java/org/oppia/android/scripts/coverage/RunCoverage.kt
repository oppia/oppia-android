package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.util.concurrent.TimeUnit

fun main(vararg args: String) {
  val repoRoot = File(args[0]).absoluteFile.normalize()
  val targetPath = args[1]

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
    RunCoverageForTestTarget(
      repoRoot,
      targetPath,
      commandExecutor,
      scriptBgDispatcher
    ).runCoverage()
  }
}