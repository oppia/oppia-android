package org.oppia.android.scripts.release

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File

/**
 * The main entrypoint for deriving the fully qualified release binary archive name for a given
 * commit.
 *
 * Reads version.bzl from the specified commit using git show and constructs the archive name
 * following the pattern: oppia-android-<major>.<minor>-rc<nn>-<flavor>-<short_sha>.aab, where
 * <short_sha> is the first 10 characters of the commit SHA.
 *
 * Fails with a non-zero exit code if:
 * - The commit does not exist (exits with an InvalidCommitException message).
 * - version.bzl is missing at the given commit (exits with a VersionParseException message).
 * - version.bzl cannot be parsed for MAJOR_VERSION or MINOR_VERSION (exits with a
 *     VersionParseException message).
 *
 * Usage:
 *   bazel run //scripts:derive_version_name_for_commit -- \\
 *     <path_to_directory_root> <commit_ref> <flavor> <rc_number>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - commit_ref: the commit SHA or branch name to derive the version from (e.g. release-0.17).
 * - flavor: the build flavor to include in the name (alpha, beta, or ga).
 * - rc_number: the release candidate number as a string (e.g. "01", "02").
 *
 * Example:
 *   bazel run //scripts:derive_version_name_for_commit -- \\
 *     $(pwd) release-0.17 beta 01
 */
fun main(vararg args: String) {
  require(args.size == 4) {
    "Usage: bazel run //scripts:derive_version_name_for_commit --" +
      " <path_to_directory_root> <commit_ref> <flavor> <rc_number>"
  }
  val repoRoot = args[0]
  val commitRef = args[1]
  val flavor = args[2]
  val rcNumber = args[3]
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val deriver = DeriveVersionNameForCommit(repoRoot, scriptBgDispatcher)
    println(deriver.computeBinaryName(commitRef, flavor, rcNumber))
  }
}

/**
 * Computes the fully qualified release binary archive name for a given commit, flavor, and RC
 * number.
 *
 * The archive name follows the format:
 *   oppia-android-<major>.<minor>-rc<nn>-<flavor>-<short_sha>.aab
 */
class DeriveVersionNameForCommit(
  private val repoRoot: String,
  scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl(scriptBgDispatcher)
) {
  /**
   * Returns the fully qualified AAB archive name for [commitRef], [flavor], and [rcNumber].
   *
   * @param commitRef the commit SHA or branch name to derive the version from
   * @param flavor the build flavor to include in the name (alpha, beta, or ga)
   * @param rcNumber the release candidate number as a string (e.g. "01")
   */
  fun computeBinaryName(commitRef: String, flavor: String, rcNumber: String): String {
    verifyCommitExists(commitRef)
    val versionBzlContent = readVersionBzl(commitRef)
    val (major, minor) = parseVersion(versionBzlContent, commitRef)
    val shortSha = resolveShortSha(commitRef)
    val paddedRc = rcNumber.padStart(2, '0')
    return "oppia-android-$major.$minor-rc$paddedRc-$flavor-$shortSha.aab"
  }

  private fun verifyCommitExists(commitRef: String) {
    val result = commandExecutor.executeCommand(
      File(repoRoot), "git", "cat-file", "-e", "$commitRef^{commit}",
      includeErrorOutput = false
    )
    check(result.exitCode == 0) {
      "InvalidCommitException: Commit does not exist: $commitRef"
    }
  }

  private fun readVersionBzl(commitRef: String): String {
    val result = commandExecutor.executeCommand(
      File(repoRoot), "git", "show", "$commitRef:version.bzl",
      includeErrorOutput = false
    )
    check(result.exitCode == 0) {
      "VersionParseException: version.bzl does not exist at commit: $commitRef"
    }
    return result.output.joinToString("\n")
  }

  private fun parseVersion(content: String, commitRef: String): Pair<Int, Int> {
    val majorMatch = Regex("""MAJOR_VERSION\s*=\s*(\d+)""").find(content)
    val minorMatch = Regex("""MINOR_VERSION\s*=\s*(\d+)""").find(content)
    check(majorMatch != null && minorMatch != null) {
      "VersionParseException: Could not parse MAJOR_VERSION or MINOR_VERSION in version.bzl" +
        " at commit: $commitRef"
    }
    return majorMatch.groupValues[1].toInt() to minorMatch.groupValues[1].toInt()
  }

  private fun resolveShortSha(commitRef: String): String {
    val result = commandExecutor.executeCommand(
      File(repoRoot), "git", "rev-parse", "--short=10", commitRef
    )
    return result.output.first().trim()
  }
}
