package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

private const val COMPUTE_ALL_FILES_PREFIX = "compute_all_files="
private const val MAX_TEST_COUNT_PER_LARGE_SHARD = 50
private const val MAX_TEST_COUNT_PER_MEDIUM_SHARD = 25
private const val MAX_TEST_COUNT_PER_SMALL_SHARD = 15

fun main(args: Array<String>) {
  val pathToRoot = args[0]
  val baseCommit = args[1]

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    ComputeChangedFiles(scriptBgDispatcher)
      .compute(pathToRoot, baseCommit)
  }
}

/** Utility used to compute changed files. */
class ComputeChangedFiles(
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  val maxTestCountPerLargeShard: Int = MAX_TEST_COUNT_PER_LARGE_SHARD,
  val maxTestCountPerMediumShard: Int = MAX_TEST_COUNT_PER_MEDIUM_SHARD,
  val maxTestCountPerSmallShard: Int = MAX_TEST_COUNT_PER_SMALL_SHARD,
  val commandExecutor: CommandExecutor =
    CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
) {
  private companion object {
    private const val GENERIC_TEST_BUCKET_NAME = "generic"
  }

  fun compute(
    pathToRoot: String,
    baseCommit: String
  ) {
    val rootDirectory = File(pathToRoot).absoluteFile
    check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
    check(rootDirectory.list()?.contains("WORKSPACE") == true) {
      "Expected script to be run from the workspace's root directory"
    }

    println("Running from directory root: $rootDirectory.")

    val gitClient = GitClient(rootDirectory, baseCommit, commandExecutor)
    println("Current branch: ${gitClient.currentBranch}.")
    println("Most recent common commit: ${gitClient.branchMergeBase}.")

    val currentBranch = gitClient.currentBranch.lowercase(Locale.US)
    println("Current Branch: $currentBranch")

    val changedFiles = computeChangedFilesForNonDevelopBranch(gitClient, rootDirectory)
    println("Changed Files: $changedFiles")
  }

  private fun computeChangedFilesForNonDevelopBranch(
    gitClient: GitClient,
    rootDirectory: File
  ): List<String> {
    val changedFiles = gitClient.changedFiles.filter { filepath ->
      File(rootDirectory, filepath).exists()
    }.toSet()
    println("Changed files (per Git, ${changedFiles.size} total): $changedFiles")

    return changedFiles.toList()
  }
}
