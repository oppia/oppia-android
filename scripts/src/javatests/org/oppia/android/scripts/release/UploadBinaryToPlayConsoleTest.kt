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

  // ---------------------------------------------------------------------------
  // runUpload — full upload flow tests (bypass gcloud via FakePlayConsoleClient)
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_noReleasesOnTrack_completesFullUploadFlow() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Bug fixes and performance improvements.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      versionName = "0.17-rc01-alpha",
      majorMinorVersion = "0.17",
      flavor = "alpha",
      track = "alpha"
    )

    assertThat(fake.createdEdits).hasSize(1)
    assertThat(fake.uploadedBundles).hasSize(1)
    assertThat(fake.trackUpdates).hasSize(1)
    assertThat(fake.committedEdits).hasSize(1)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsCorrectAabPath() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      versionName = "0.17-rc01-alpha",
      majorMinorVersion = "0.17",
      flavor = "alpha",
      track = "alpha"
    )

    val (_, _, path) = fake.uploadedBundles.first()
    assertThat(path).isEqualTo(aab.absolutePath)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_assignsReturnedVersionCodeToTrack() {
    val fake = FakePlayConsoleClient()
    fake.setNextVersionCode(301L)
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      versionName = "0.17-rc01-alpha",
      majorMinorVersion = "0.17",
      flavor = "alpha",
      track = "alpha"
    )

    val update = fake.trackUpdates.first()
    assertThat(update.versionCode).isEqualTo(301L)
    assertThat(update.track).isEqualTo("alpha")
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsChangelogAsReleaseNotes() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "User-facing release notes.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      versionName = "0.17-rc01-alpha",
      majorMinorVersion = "0.17",
      flavor = "alpha",
      track = "alpha"
    )

    val update = fake.trackUpdates.first()
    assertThat(update.releaseNotes["en-US"]).isEqualTo("User-facing release notes.")
  }

  @Test
  fun testRunUpload_pendingReleaseOnTrack_throwsBeforeUpload() {
    val fake = FakePlayConsoleClient()
    fake.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(299L), status = "draft"))
    )
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        versionName = "0.17-rc01-alpha",
        majorMinorVersion = "0.17",
        flavor = "alpha",
        track = "alpha"
      )
    }

    // Upload must NOT have been attempted.
    assertThat(fake.uploadedBundles).isEmpty()
  }

  @Test
  fun testRunUpload_changelogFileMissing_throwsBeforeUpload() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    // No changelog file created.

    assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        versionName = "0.17-rc01-alpha",
        majorMinorVersion = "0.17",
        flavor = "alpha",
        track = "alpha"
      )
    }

    assertThat(fake.uploadedBundles).isEmpty()
  }

  @Test
  fun testRunUpload_versionInversionDetected_throwsAfterUploadNotCommitted() {
    val fake = FakePlayConsoleClient()
    // A completed release already has version code 500, so uploading vc=1 would be an inversion.
    fake.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(500L), status = "completed")
      )
    )
    fake.setNextVersionCode(1L)
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        versionName = "0.17-rc01-alpha",
        majorMinorVersion = "0.17",
        flavor = "alpha",
        track = "alpha"
      )
    }

    // AAB was uploaded but the edit was never committed.
    assertThat(fake.uploadedBundles).hasSize(1)
    assertThat(fake.committedEdits).isEmpty()
  }

  // ---------------------------------------------------------------------------
  // extractReleaseNotes — changelog file resolution
  // ---------------------------------------------------------------------------

  @Test
  fun testExtractReleaseNotes_defaultFileExists_returnsEnUsContent() {
    createChangelog("0.17", content = "Bug fixes.")

    val notes = extractReleaseNotes(tempFolder.root.absolutePath, "0.17", "alpha")

    assertThat(notes["en-US"]).isEqualTo("Bug fixes.")
  }

  @Test
  fun testExtractReleaseNotes_flavorSpecificFileExists_usesFlavorFileOverDefault() {
    createChangelog("0.17", content = "Default notes.")
    createChangelog("0.17", flavor = "alpha", content = "Alpha-specific notes.")

    val notes = extractReleaseNotes(tempFolder.root.absolutePath, "0.17", "alpha")

    assertThat(notes["en-US"]).isEqualTo("Alpha-specific notes.")
  }

  @Test
  fun testExtractReleaseNotes_noFileExists_returnsEmptyMap() {
    // No changelog files created.
    val notes = extractReleaseNotes(tempFolder.root.absolutePath, "0.17", "alpha")

    assertThat(notes).isEmpty()
  }

  @Test
  fun testExtractReleaseNotes_emptyDefaultFile_returnsEmptyMap() {
    createChangelog("0.17", content = "")

    val notes = extractReleaseNotes(tempFolder.root.absolutePath, "0.17", "alpha")

    assertThat(notes).isEmpty()
  }

  @Test
  fun testExtractReleaseNotes_contentExceeds500Chars_truncatesToMaxLength() {
    val longContent = "a".repeat(600)
    createChangelog("0.17", content = longContent)

    val notes = extractReleaseNotes(tempFolder.root.absolutePath, "0.17", "alpha")

    assertThat(notes["en-US"]).hasLength(500)
  }

  @Test
  fun testExtractReleaseNotes_contentExactly500Chars_notTruncated() {
    val exactContent = "b".repeat(500)
    createChangelog("0.17", content = exactContent)

    val notes = extractReleaseNotes(tempFolder.root.absolutePath, "0.17", "alpha")

    assertThat(notes["en-US"]).hasLength(500)
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Creates a `config/changelogs/<version>[_<flavor>].md` file in the temp folder.
   *
   * @param version the major.minor version string (e.g. "0.17")
   * @param flavor optional flavor suffix (e.g. "alpha") — omit for the default changelog
   * @param content the text content to write to the file
   */
  private fun createChangelog(version: String, flavor: String? = null, content: String): File {
    val dir = File(tempFolder.root, "config/changelogs").also { it.mkdirs() }
    val filename = if (flavor != null) "${version}_$flavor.md" else "$version.md"
    return File(dir, filename).also { it.writeText(content) }
  }
}
