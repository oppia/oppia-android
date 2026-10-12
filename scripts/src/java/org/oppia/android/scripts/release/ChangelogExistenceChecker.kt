package org.oppia.android.scripts.release

import java.io.File

/**
 * Precondition checker that verifies a changelog file exists for the release version being
 * uploaded.
 *
 * Changelogs live in [CHANGELOGS_DIR] and are named by their major.minor version (e.g. `0.17.md`).
 * A flavor-specific override (e.g. `0.17_beta.md`) is checked first and falls back to the default.
 * Deployment is blocked if neither file exists.
 *
 * This mirrors the lookup order used by `UploadChangelogToPlayConsole`:
 * 1. `config/changelogs/<majorVersion>.<minorVersion>_<flavor>.md` (flavor-specific override)
 * 2. `config/changelogs/<majorVersion>.<minorVersion>.md` (default)
 * 3. Fail if neither exists
 */
class ChangelogExistenceChecker(private val workspaceRoot: String) {

  /**
   * Verifies that a changelog file exists for [majorVersion].[minorVersion] and [flavor].
   *
   * @param majorVersion the major version number (e.g. 0)
   * @param minorVersion the minor version number (e.g. 17)
   * @param flavor the build flavor
   * @throws IllegalStateException if no changelog file is found for the given version and flavor
   */
  fun verify(majorVersion: Int, minorVersion: Int, flavor: AppFlavor) {
    val majorMinorVersion = "$majorVersion.$minorVersion"
    val flavorId = flavor.id
    val changelogsDir = File(workspaceRoot, CHANGELOGS_DIR)

    check(changelogsDir.exists() && changelogsDir.isDirectory) {
      "Changelogs directory not found at '${changelogsDir.absolutePath}'. " +
        "Every release must have a corresponding changelog in $CHANGELOGS_DIR."
    }

    // Check flavor-specific override first, then fall back to the default changelog.
    val flavorSpecificFile = File(changelogsDir, "${majorMinorVersion}_$flavorId.md")
    val defaultFile = File(changelogsDir, "$majorMinorVersion.md")

    checkNotNull(
      when {
        flavorSpecificFile.exists() -> flavorSpecificFile
        defaultFile.exists() -> defaultFile
        else -> null
      }
    ) {
      "No changelog found for version '$majorMinorVersion' (flavor '$flavorId'). " +
        "Expected one of:\n" +
        "  ${flavorSpecificFile.absolutePath}\n" +
        "  ${defaultFile.absolutePath}"
    }
  }

  private companion object {
    private const val CHANGELOGS_DIR = "config/changelogs"
  }
}
