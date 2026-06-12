package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File

/** Tests for [ChangelogExistenceCheck]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ChangelogExistenceCheckTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private fun changelogsDir(): File =
    tempFolder.newFolder("config", "changelogs")

  private fun check(): ChangelogExistenceCheck =
    ChangelogExistenceCheck(tempFolder.root.absolutePath)

  // ---------------------------------------------------------------------------
  // Missing changelogs directory
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_missingChangelogsDir_throwsWithDirPath() {
    // Do NOT create the changelogs directory.
    val exception = assertThrows<IllegalStateException>() {
      check().verify("0.17", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("Changelogs directory not found")
    assertThat(exception).hasMessageThat().contains("config/changelogs")
  }

  // ---------------------------------------------------------------------------
  // Default changelog (no flavor override)
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_defaultChangelogExists_passes() {
    val dir = changelogsDir()
    File(dir, "0.17.md").writeText("## 0.17\n- Feature A")

    // Should not throw.
    check().verify("0.17", "beta")
  }

  @Test
  fun testVerify_defaultChangelog_differentVersion_passes() {
    val dir = changelogsDir()
    File(dir, "1.0.md").writeText("## 1.0\n- Major release")

    check().verify("1.0", "ga")
  }

  // ---------------------------------------------------------------------------
  // Flavor-specific override takes precedence
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_flavorOverrideExists_usesOverrideNotDefault() {
    val dir = changelogsDir()
    File(dir, "0.17.md").writeText("Default changelog")
    File(dir, "0.17_alpha.md").writeText("Alpha-specific changelog")

    // Should not throw — override is found first.
    check().verify("0.17", "alpha")
  }

  @Test
  fun testVerify_flavorOverrideExistsButDefaultMissing_passes() {
    val dir = changelogsDir()
    // Only the flavor-specific file exists, no default.
    File(dir, "0.17_beta.md").writeText("Beta-only changelog")

    check().verify("0.17", "beta")
  }

  @Test
  fun testVerify_gaFlavorOverride_passes() {
    val dir = changelogsDir()
    File(dir, "0.18_ga.md").writeText("GA-specific changelog")

    check().verify("0.18", "ga")
  }

  // ---------------------------------------------------------------------------
  // Missing changelog → failure
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_noChangelogFound_throwsWithVersionAndFlavor() {
    changelogsDir() // directory exists but is empty

    val exception = assertThrows<IllegalStateException>() {
      check().verify("0.17", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("No changelog found")
    assertThat(exception).hasMessageThat().contains("0.17")
    assertThat(exception).hasMessageThat().contains("alpha")
  }

  @Test
  fun testVerify_wrongVersionPresent_throwsForRequestedVersion() {
    val dir = changelogsDir()
    File(dir, "0.16.md").writeText("Old changelog") // different version

    val exception = assertThrows<IllegalStateException>() {
      check().verify("0.17", "beta")
    }

    assertThat(exception).hasMessageThat().contains("0.17")
  }

  @Test
  fun testVerify_flavorOverrideForDifferentFlavor_usesDefault() {
    val dir = changelogsDir()
    File(dir, "0.17.md").writeText("Default")
    File(dir, "0.17_alpha.md").writeText("Alpha override")

    // Requesting "beta" — alpha override doesn't apply, but default exists.
    check().verify("0.17", "beta")
  }

  @Test
  fun testVerify_flavorOverrideForDifferentFlavor_noDefault_throws() {
    val dir = changelogsDir()
    File(dir, "0.17_alpha.md").writeText("Alpha override only")

    // Requesting "beta" — no beta override, no default.
    val exception = assertThrows<IllegalStateException>() {
      check().verify("0.17", "beta")
    }

    assertThat(exception).hasMessageThat().contains("No changelog found")
    assertThat(exception).hasMessageThat().contains("0.17")
    assertThat(exception).hasMessageThat().contains("beta")
  }
}
