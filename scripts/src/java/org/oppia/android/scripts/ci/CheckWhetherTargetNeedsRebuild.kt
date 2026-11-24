package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * Files that can affect any build without being part of a target's dependency graph, so changing
 * any of them requires every target to be rebuilt. Note that .bzl and .patch files are always
 * treated as build-wide since they may be used by WORKSPACE.
 */
private val BUILD_WIDE_FILES = setOf(
  "WORKSPACE",
  ".bazelrc",
  ".bazelversion",
  "third_party/maven_install.json",
  ".github/workflows/build_tests.yml",
  ".github/actions/set-up-android-bazel-build-environment/action.yml"
)

/**
 * The main entrypoint for checking whether a Bazel target needs to be rebuilt based on changes in
 * the local Oppia Android Git repository.
 *
 * The script exits with 0 if the target does not need to be rebuilt, and with 1 if it does. Note
 * that any failure also results in an exit code of 1, so that callers err on the side of
 * rebuilding.
 *
 * Usage:
 *   bazel run //scripts:check_whether_target_needs_rebuild -- \\
 *     <path_to_directory_root> <merge_base_commit> <bazel_target>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - merge_base_commit: the base commit against which the local changes will be compared. When
 *     running outside of CI you can use the result of running: 'git merge-base develop HEAD'
 * - bazel_target: the Bazel target to check, e.g. //:oppia_dev.
 *
 * Example:
 *   bazel run //scripts:check_whether_target_needs_rebuild -- $(pwd) abcdef0123456789 //:oppia_dev
 */
fun main(args: Array<String>) {
  if (args.size < 3) {
    println(
      "Usage: bazel run //scripts:check_whether_target_needs_rebuild --" +
        " <path_to_directory_root> <merge_base_commit> <bazel_target>"
    )
    exitProcess(1)
  }

  val needsRebuild = ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    CheckWhetherTargetNeedsRebuild(scriptBgDispatcher)
      .needsRebuild(pathToRoot = args[0], baseCommit = args[1], bazelTarget = args[2])
  }
  exitProcess(if (needsRebuild) 1 else 0)
}

/** Utility used to check whether a Bazel target needs to be rebuilt. */
class CheckWhetherTargetNeedsRebuild(
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val commandExecutor: CommandExecutor =
    CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
) {
  /**
   * Returns whether [bazelTarget] needs to be rebuilt.
   *
   * A rebuild is needed when on the develop branch, when a build-wide file (such as WORKSPACE, or
   * any .bzl or .patch file) changed, or when a changed file is part of the target's dependency
   * graph (including the BUILD and .bzl files defining it). Deleted files are no longer part of the
   * graph, so they're treated as affecting the target if they were in a directory with any of its
   * files (e.g. a file matched by a glob).
   *
   * @param pathToRoot the absolute path to the working root directory
   * @param baseCommit see [GitClient]
   * @param bazelTarget the Bazel target to check, e.g. //:oppia_dev
   */
  fun needsRebuild(pathToRoot: String, baseCommit: String, bazelTarget: String): Boolean {
    val rootDirectory = File(pathToRoot).absoluteFile
    check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
    check(rootDirectory.list()?.contains("WORKSPACE") == true) {
      "Expected script to be run from the workspace's root directory"
    }

    val gitClient = GitClient(rootDirectory, baseCommit, commandExecutor)
    if (gitClient.currentBranch.lowercase(Locale.US) == "develop") {
      println("$bazelTarget needs to be rebuilt since it's always rebuilt on develop.")
      return true
    }

    // Committed files are included since they contain both paths of renamed files.
    val changedFiles = gitClient.changedFiles + gitClient.committedFiles
    println("Changed files (per Git, ${changedFiles.size} total): $changedFiles.")

    val changedBuildWideFiles = changedFiles.filter { filePath ->
      filePath in BUILD_WIDE_FILES || filePath.endsWith(".bzl") || filePath.endsWith(".patch")
    }
    if (changedBuildWideFiles.isNotEmpty()) {
      println("$bazelTarget needs to be rebuilt due to build-wide files: $changedBuildWideFiles.")
      return true
    }

    val targetFiles = BazelClient(rootDirectory, commandExecutor)
      .retrieveTransitiveWorkspaceFiles(bazelTarget).toSet()
    val targetDirectories = targetFiles.mapTo(mutableSetOf()) { it.substringBeforeLast('/', "") }
    val affectingFiles = changedFiles.filter { filePath ->
      if (File(rootDirectory, filePath).exists()) {
        filePath in targetFiles
      } else filePath.substringBeforeLast('/', "") in targetDirectories
    }
    return if (affectingFiles.isNotEmpty()) {
      println("$bazelTarget needs to be rebuilt due to changed files: $affectingFiles.")
      true
    } else {
      println("$bazelTarget doesn't need to be rebuilt since no changed files affect it.")
      false
    }
  }
}
