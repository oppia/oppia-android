package org.oppia.android.scripts.release

import java.io.File

/**
 * Precondition check that verifies a changelog file exists for the release version being uploaded.
 *
 * Changelogs live in [CHANGELOGS_DIR] and are named by their major.minor version (e.g. `0.17.md`).
 * A flavor-specific override (e.g. `0.17_beta.md`) is checked first and falls back to the default.
 * Deployment is blocked if neither file exists.
 *
 * This mirrors the lookup order used by `UploadChangelogToPlayConsole`:
 * 1. `config/changelogs/<major.minor>_<flavor>.md` (flavor-specific override)
 * 2. `config/changelogs/<major.minor>.md` (default)
 * 3. Fail if neither exists
 */
class ChangelogExistenceCheck(private val workspaceRoot: String) {

  /**
   * Verifies that a changelog file exists for [majorMinorVersion] and [flavor].
   *
   * @param majorMinorVersion the major.minor version string (e.g. "0.17")
   * @param flavor the build flavor (e.g. "alpha", "beta", "ga")
   * @throws IllegalStateException if no changelog file is found for the given version and flavor
   */
  fun verify(majorMinorVersion: String, flavor: String) {
    val changelogsDir = File(workspaceRoot, CHANGELOGS_DIR)

    check(changelogsDir.exists() && changelogsDir.isDirectory) {
      "Changelogs directory not found at '${changelogsDir.absolutePath}'. " +
        "Every release must have a corresponding changelog in $CHANGELOGS_DIR."
    }

    // Check flavor-specific override first, then fall back to the default changelog.
    val flavorSpecificFile = File(changelogsDir, "${majorMinorVersion}_$flavor.md")
    val defaultFile = File(changelogsDir, "$majorMinorVersion.md")

    val resolvedFile = when {
      flavorSpecificFile.exists() -> flavorSpecificFile
      defaultFile.exists() -> defaultFile
      else -> null
    }

    check(resolvedFile != null) {
      "No changelog found for version '$majorMinorVersion' (flavor '$flavor'). " +
        "Expected one of:\n" +
        "  ${flavorSpecificFile.absolutePath}\n" +
        "  ${defaultFile.absolutePath}"
    }

    println(
      "Changelog existence check passed: using '${resolvedFile.name}' " +
        "for version '$majorMinorVersion' ($flavor)."
    )
  }

  private companion object {
    private const val CHANGELOGS_DIR = "config/changelogs"
  }
}
