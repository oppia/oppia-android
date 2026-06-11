package org.oppia.android.scripts.release

import java.io.File

/**
 * Precondition check that verifies a changelog file exists for the release version being uploaded.
 *
 * Each release must be accompanied by a changelog entry so that Play Console release notes can
 * be populated and the release is traceable in the repository. The changelog is expected to live
 * at [CHANGELOG_RELATIVE_PATH] within the workspace root and must contain a section header
 * matching the given version string (e.g. "## 0.18-rc00").
 */
class ChangelogExistenceCheck(private val workspaceRoot: String) {

  /**
   * Verifies that a changelog entry exists for [versionName] within the repository's changelog
   * file.
   *
   * @param versionName the full version string to look for in the changelog
   *     (e.g. "0.18-rc00-alpha")
   * @throws IllegalStateException if the changelog file does not exist or contains no section
   *     header for [versionName]
   */
  fun verify(versionName: String) {
    val changelogFile = File(workspaceRoot, CHANGELOG_RELATIVE_PATH)

    check(changelogFile.exists()) {
      "Changelog file not found at '${changelogFile.absolutePath}'. " +
        "Every release must have a corresponding changelog entry."
    }

    val changelogContent = changelogFile.readText()
    val sectionHeader = "## $versionName"

    check(changelogContent.contains(sectionHeader)) {
      "No changelog entry found for version '$versionName' in '${changelogFile.absolutePath}'. " +
        "Expected a section starting with '$sectionHeader'."
    }

    println("Changelog existence check passed: found entry for '$versionName'.")
  }

  private companion object {
    private const val CHANGELOG_RELATIVE_PATH = "CHANGELOG.md"
  }
}
