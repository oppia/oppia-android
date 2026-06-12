package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for the upload_binary_to_play_console script.
 *
 * Tests cover argument validation and AAB filename parsing. The full upload flow (edit session →
 * AAB upload → track assignment → commit) is not exercised here because it requires live GCP
 * credentials; that layer is covered by the dedicated integration tests for each precondition
 * check class ([VersionInversionCheckTest], [PendingReleaseCheckTest],
 * [ChangelogExistenceCheckTest]).
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UploadBinaryToPlayConsoleTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private fun runScript(vararg args: String) {
    main(args.toList().toTypedArray())
  }

  // Helper to create a valid dummy AAB file in the temp folder.
  private fun createAab(name: String): File =
    tempFolder.newFile(name).also { it.writeBytes(ByteArray(64)) }

  // ---------------------------------------------------------------------------
  // Argument count validation
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_noArgs_throwsWithUsageHint() {
    val exception = assertThrows<IllegalArgumentException>() { runScript() }

    assertThat(exception).hasMessageThat().contains("Usage:")
    assertThat(exception).hasMessageThat().contains("upload_binary_to_play_console")
  }

  @Test
  fun testScript_tooFewArgs_throwsWithUsageHint() {
    val exception = assertThrows<IllegalArgumentException>() {
      runScript("workspace", "aab.aab", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testScript_tooManyArgs_throwsWithUsageHint() {
    val exception = assertThrows<IllegalArgumentException>() {
      runScript("a", "b", "c", "d", "e", "extra")
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  // ---------------------------------------------------------------------------
  // Rollout fraction validation
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_nonNumericRolloutFraction_throwsWithMessage() {
    val exception = assertThrows<IllegalArgumentException>() {
      runScript("workspace", "aab.aab", "alpha", "project", "notanumber")
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction")
  }

  @Test
  fun testScript_rolloutFractionAboveOne_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.1")
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction")
    assertThat(exception).hasMessageThat().contains("0.0")
    assertThat(exception).hasMessageThat().contains("1.0")
  }

  @Test
  fun testScript_negativeRolloutFraction_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-beta-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "beta", "project", "-0.1")
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction")
  }

  // ---------------------------------------------------------------------------
  // Track validation
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_invalidTrack_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "internal", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().contains("track")
    assertThat(exception).hasMessageThat().contains("internal")
  }

  @Test
  fun testScript_emptyTrack_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().contains("track")
  }

  // ---------------------------------------------------------------------------
  // AAB file existence
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_missingAabFile_throwsWithPath() {
    val exception = assertThrows<IllegalArgumentException>() {
      runScript(
        tempFolder.root.absolutePath,
        "/tmp/nonexistent-oppia.aab",
        "alpha",
        "project",
        "1.0"
      )
    }

    assertThat(exception).hasMessageThat().contains("AAB file not found")
  }

  // ---------------------------------------------------------------------------
  // AAB filename parsing — invalid filenames
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_aabWithMalformedName_throwsWithExpectedFormatHint() {
    val aab = createAab("my-app.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().contains("Cannot extract version name")
    assertThat(exception).hasMessageThat().contains("my-app.aab")
  }

  @Test
  fun testScript_aabWithoutOppiaPrefix_throwsWithMessage() {
    val aab = createAab("app-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().contains("Cannot extract version name")
  }

  @Test
  fun testScript_aabWithInvalidFlavor_throwsWithMessage() {
    // "gamma" is not a valid flavor — only alpha/beta/ga are accepted.
    val aab = createAab("oppia-android-0.17-rc01-gamma-e740815230.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().contains("Cannot extract version name")
  }

  @Test
  fun testScript_aabWithMissingHash_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().contains("Cannot extract version name")
  }

  // ---------------------------------------------------------------------------
  // AAB filename parsing — valid filenames pass parsing (fail later at gcloud)
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_validAlphaAabName_passesParsingFailsAtGcloud() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    // Should fail at obtainAccessToken (gcloud not available in unit tests), not at parsing.
    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.0")
    }

    // The error must NOT be a parsing error.
    assertThat(exception).hasMessageThat().doesNotContain("Cannot extract version name")
    assertThat(exception).hasMessageThat().doesNotContain("Cannot extract flavor")
  }

  @Test
  fun testScript_validBetaAabName_passesParsingFailsAtGcloud() {
    val aab = createAab("oppia-android-0.18-rc02-beta-abc1234567.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "beta", "project", "0.25")
    }

    assertThat(exception).hasMessageThat().doesNotContain("Cannot extract version name")
    assertThat(exception).hasMessageThat().doesNotContain("Cannot extract flavor")
  }

  @Test
  fun testScript_validGaAabName_passesParsingFailsAtGcloud() {
    val aab = createAab("oppia-android-1.0-rc01-ga-deadbeef12.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "production", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().doesNotContain("Cannot extract version name")
    assertThat(exception).hasMessageThat().doesNotContain("Cannot extract flavor")
  }

  // ---------------------------------------------------------------------------
  // Boundary rollout fractions — valid values pass arg checks
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_rolloutFractionZero_passesValidationFailsAtGcloud() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "0.0")
    }

    // 0.0 is a valid fraction — failure must be after arg validation.
    assertThat(exception).hasMessageThat().doesNotContain("rollout_fraction")
  }

  @Test
  fun testScript_rolloutFractionOne_passesValidationFailsAtGcloud() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "project", "1.0")
    }

    assertThat(exception).hasMessageThat().doesNotContain("rollout_fraction")
  }
}
