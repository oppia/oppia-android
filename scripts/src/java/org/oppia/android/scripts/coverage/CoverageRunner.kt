package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import java.io.File

class CoverageRunner {
  fun runWithCoverageAsync(
    repoRoot: File,
    scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
    bazelTestTarget: String
  ): Deferred<Unit> {
    return CoroutineScope(scriptBgDispatcher).async {
      val coverageData = getCoverage(repoRoot, scriptBgDispatcher, bazelTestTarget)
      val data = coverageData.await()
      parseData(data)
    }
  }

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

  fun parseData(data: List<String>) {
    // Implementation to parse the coverage data file path String
    println("Parsed Data: $data")
  }
}





























