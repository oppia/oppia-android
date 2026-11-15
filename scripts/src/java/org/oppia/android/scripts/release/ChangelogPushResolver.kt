package org.oppia.android.scripts.release

import java.io.File

/**
 * Resolver that evaluates changed files from a Git push to determine whether a changelog
 * upload should be scheduled, skipped, or rejected.
 *
 * Rules:
 * 1. Version-bump guard: If `version.bzl` was modified in this push, skip deployment with an
 *    explanation (release-freeze commit).
 * 2. Unreleased base changelogs: An added base changelog (`config/changelogs/<version>.md` without
 *    flavor suffix) represents an unreleased version and is skipped.
 * 3. Deleted changelogs: Files with status `"removed"` are skipped (never uploaded).
 * 4. Renamed changelogs: Files with status `"renamed"` require manual dispatch and are skipped.
 * 5. Flavor overrides: Added flavor-specific changelogs (`<version>_<flavor>.md`) are eligible
 *    only if their base version was NOT also newly added in this push.
 * 6. Modified changelogs: Existing changelog files with status `"modified"` are eligible.
 * 7. Grouping: Generic and flavor-specific changelogs for the same version (e.g. `0.19.md` and
 *    `0.19_alpha.md`) are grouped together under a single version.
 * 8. Multi-version rejection: If multiple distinct versions are eligible in the same push
 *    (e.g. `0.18` and `0.19`), automatic deployment fails with a clear message instructing the
 *    user to dispatch uploads manually per version.
 */
class ChangelogPushResolver {

  /** Represents a changed file in a push with its relative path and Git change status. */
  data class ChangedFile(
    val path: String,
    val status: String
  )

  /** Sealed class representing the resolution outcome for a push. */
  sealed class ResolutionResult {
    /**
     * The push contains eligible changelog files for a single version.
     *
     * @property version the target version in major.minor format (e.g. "0.19")
     * @property files the list of eligible changelog file paths grouped under this version
     */
    data class Scheduled(
      val version: String,
      val files: List<String>
    ) : ResolutionResult()

    /**
     * The push does not contain any eligible changelog files or was skipped by an eligibility guard.
     *
     * @property reason human-readable explanation of why the upload was skipped
     */
    data class Skipped(
      val reason: String
    ) : ResolutionResult()

    /**
     * The push cannot be safely resolved automatically and failed with a fatal error.
     *
     * @property errorMessage actionable error message explaining why the run failed and what to do
     */
    data class Failed(
      val errorMessage: String
    ) : ResolutionResult()
  }

  /**
   * Resolves the changelog deployment action for a push given changed files and whether
   * `version.bzl` was modified.
   */
  fun resolve(
    versionBzlChanged: Boolean,
    changedFiles: List<ChangedFile>
  ): ResolutionResult {
    if (versionBzlChanged) {
      return ResolutionResult.Skipped(
        "version.bzl was changed in this push (version-bump/release-freeze commit). " +
          "Skipping automatic changelog upload — use workflow_dispatch after the release is live."
      )
    }

    val changelogFiles = changedFiles.filter {
      it.path.startsWith(CHANGELOGS_DIR) && it.path.endsWith(".md")
    }

    if (changelogFiles.isEmpty()) {
      return ResolutionResult.Skipped("No changelog files changed in this push.")
    }

    // Identify newly added base changelogs (e.g. config/changelogs/<version>.md with status "added").
    val newlyAddedBaseVersions = changelogFiles
      .filter { it.status == "added" && isBaseChangelog(it.path) }
      .mapNotNull { extractVersion(it.path) }
      .toSet()

    val eligibleFilesByVersion = mutableMapOf<String, MutableList<String>>()

    for (file in changelogFiles) {
      val version = extractVersion(file.path) ?: continue
      val flavor = extractFlavor(file.path)

      val isEligible = when (file.status) {
        "modified" -> true
        "added" -> {
          if (flavor == null) {
            // Newly added base changelog is an unreleased version.
            false
          } else {
            // Flavor-specific file is only eligible if its base version is not also newly added.
            version !in newlyAddedBaseVersions
          }
        }
        "removed", "renamed" -> false
        else -> false
      }

      if (isEligible) {
        eligibleFilesByVersion.getOrPut(version) { mutableListOf() }.add(file.path)
      }
    }

    return when {
      eligibleFilesByVersion.isEmpty() -> {
        ResolutionResult.Skipped(
          "No eligible changelog files found for automatic upload in this push."
        )
      }
      eligibleFilesByVersion.size > 1 -> {
        val versions = eligibleFilesByVersion.keys.sorted()
        ResolutionResult.Failed(
          "Multiple distinct changelog versions detected in this push: $versions. " +
            "Automatic upload does not support multi-version pushes to prevent release " +
            "inconsistencies. Please trigger deployment manually via workflow_dispatch for each " +
            "version individually."
        )
      }
      else -> {
        val (version, files) = eligibleFilesByVersion.entries.first()
        ResolutionResult.Scheduled(version = version, files = files)
      }
    }
  }

  companion object {
    private const val CHANGELOGS_DIR = "config/changelogs/"
    private val VERSION_REGEX = Regex("""^(\d+\.\d+)""")

    fun extractVersion(path: String): String? {
      val filename = File(path).nameWithoutExtension
      return VERSION_REGEX.find(filename)?.groupValues?.get(1)
    }

    fun extractFlavor(path: String): String? {
      val filename = File(path).nameWithoutExtension
      val version = extractVersion(path) ?: return null
      val suffix = filename.removePrefix(version)
      return if (suffix.startsWith("_")) suffix.removePrefix("_") else null
    }

    fun isBaseChangelog(path: String): Boolean {
      return extractFlavor(path) == null
    }
  }
}
