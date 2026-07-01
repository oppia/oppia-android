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
 * Tests cover argument validation, the full upload flow, and changelog resolution. The full
 * upload flow is exercised via [runUpload] with a [FakePlayConsoleClient]. Changelog content
 * behaviour (flavor resolution, length enforcement) is tested through [runUpload] as well.
 * Individual precondition checkers are tested in their own dedicated test files:
 * [VersionInversionCheckerTest], [PendingReleaseCheckerTest], [ChangelogExistenceCheckerTest].
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

  // Helper to build AabProperties for a 0.17-rc01-alpha AAB.
  private fun alphaProperties() = AabProperties(
    majorVersion = 0, minorVersion = 17, rcNumber = "01",
    flavor = AppFlavor.ALPHA,
    versionName = "0.17-rc01-alpha",
    majorMinorVersion = "0.17"
  )

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
  // Rollout fraction validation (integer in [0, 1000])
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_nonNumericRolloutFraction_throwsWithMessage() {
    val exception = assertThrows<IllegalArgumentException>() {
      runScript("workspace", "aab.aab", "alpha", "token", "notanumber")
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction")
  }

  @Test
  fun testScript_rolloutFractionAboveThousand_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "token", "1001")
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction")
    assertThat(exception).hasMessageThat().contains("1000")
  }

  @Test
  fun testScript_negativeRolloutFraction_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-beta-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "beta", "token", "-1")
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction")
  }

  @Test
  fun testScript_decimalRolloutFraction_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "token", "0.5")
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
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "internal", "token", "1000")
    }

    assertThat(exception).hasMessageThat().contains("track")
    assertThat(exception).hasMessageThat().contains("internal")
  }

  @Test
  fun testScript_emptyTrack_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalArgumentException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "", "token", "1000")
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
        "token",
        "1000"
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
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "token", "1000")
    }

    assertThat(exception).hasMessageThat().contains("Cannot parse AAB filename")
    assertThat(exception).hasMessageThat().contains("my-app.aab")
  }

  @Test
  fun testScript_aabWithoutOppiaPrefix_throwsWithMessage() {
    val aab = createAab("app-0.17-rc01-alpha-e740815230.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "token", "1000")
    }

    assertThat(exception).hasMessageThat().contains("Cannot parse AAB filename")
  }

  @Test
  fun testScript_aabWithInvalidFlavor_throwsWithMessage() {
    // "gamma" is not a valid flavor — only alpha/beta/ga are accepted.
    val aab = createAab("oppia-android-0.17-rc01-gamma-e740815230.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "token", "1000")
    }

    assertThat(exception).hasMessageThat().contains("Cannot parse AAB filename")
  }

  @Test
  fun testScript_aabWithMissingHash_throwsWithMessage() {
    val aab = createAab("oppia-android-0.17-rc01-alpha.aab")

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, aab.absolutePath, "alpha", "token", "1000")
    }

    assertThat(exception).hasMessageThat().contains("Cannot parse AAB filename")
  }

  // ---------------------------------------------------------------------------
  // Boundary rollout fractions — valid values complete the full upload flow
  // ---------------------------------------------------------------------------

  @Test
  fun testScript_rolloutFractionZero_passesValidation() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    // rollout_fraction = 0 is valid — the full upload flow should complete without errors.
    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 0
    )

    assertThat(fake.uploadedBundles).hasSize(1)
  }

  @Test
  fun testScript_rolloutFractionThousand_passesValidation() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    // rollout_fraction = 1000 (100%) is valid — the full upload flow should complete.
    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
    )

    assertThat(fake.uploadedBundles).hasSize(1)
  }


  @Test
  fun testRunUpload_noReleasesOnTrack_completesFullUploadFlow() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Bug fixes and performance improvements.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
    )

    assertThat(fake.createdEdits).isNotEmpty()
    assertThat(fake.uploadedBundles).hasSize(1)
    assertThat(fake.trackUpdates).hasSize(1)
    assertThat(fake.committedEdits).isNotEmpty()
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
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
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
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
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
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
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

    val exception = assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        properties = alphaProperties(),
        track = "alpha",
        rolloutFraction = 1000
      )
    }

    assertThat(exception).hasMessageThat().contains("Pending release detected")
    // Upload must NOT have been attempted.
    assertThat(fake.uploadedBundles).isEmpty()
  }

  @Test
  fun testRunUpload_changelogFileMissing_throwsBeforeUpload() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    // No changelog file created.

    val exception = assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        properties = alphaProperties(),
        track = "alpha",
        rolloutFraction = 1000
      )
    }

    assertThat(exception).hasMessageThat().contains("changelog")
    assertThat(fake.uploadedBundles).isEmpty()
  }

  @Test
  fun testRunUpload_crossTrackInversion_throwsAfterUploadNotCommitted() {
    val fake = FakePlayConsoleClient()
    // Beta has vc=500; deploying vc=1 to alpha would violate alpha > beta.
    fake.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(500L), status = "completed"))
    )
    fake.setNextVersionCode(1L)
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    val exception = assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        properties = alphaProperties(),
        track = "alpha",
        rolloutFraction = 1000
      )
    }

    assertThat(exception).hasMessageThat().contains("Version inversion")
    // AAB was uploaded but the edit was never committed.
    assertThat(fake.uploadedBundles).hasSize(1)
    assertThat(fake.committedEdits).isEmpty()
  }

  // ---------------------------------------------------------------------------
  // runUpload — rollout fraction forwarding
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_fullRollout_recordsRolloutFractionAsThousand() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
    )

    assertThat(fake.trackUpdates.first().rolloutFraction).isEqualTo(1000)
  }

  @Test
  fun testRunUpload_stagedRollout_recordsCorrectRolloutFraction() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 250
    )

    assertThat(fake.trackUpdates.first().rolloutFraction).isEqualTo(250)
  }

  // ---------------------------------------------------------------------------
  // runUpload — changelog content behaviour
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_flavorSpecificChangelogExists_usesFlavorFileOverDefault() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Default notes.")
    createChangelog("0.17", flavor = "alpha", content = "Alpha-specific notes.")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
    )

    assertThat(fake.trackUpdates.first().releaseNotes["en-US"]).isEqualTo("Alpha-specific notes.")
  }

  @Test
  fun testRunUpload_emptyChangelog_setsEmptyReleaseNotes() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "")

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
    )

    assertThat(fake.trackUpdates.first().releaseNotes).isEmpty()
  }

  @Test
  fun testRunUpload_changelogExceeds500Chars_throwsBeforeSetTrackRelease() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "a".repeat(600))

    val exception = assertThrows<IllegalStateException>() {
      runUpload(
        client = fake,
        workspaceRoot = tempFolder.root.absolutePath,
        aabPath = aab.absolutePath,
        properties = alphaProperties(),
        track = "alpha",
        rolloutFraction = 1000
      )
    }

    assertThat(exception).hasMessageThat().contains("500")
    assertThat(exception).hasMessageThat().contains("600")
    // Track release must NOT have been set.
    assertThat(fake.trackUpdates).isEmpty()
  }

  @Test
  fun testRunUpload_changelogExactly500Chars_completesUploadWithFullNotes() {
    val fake = FakePlayConsoleClient()
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "b".repeat(500))

    runUpload(
      client = fake,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = alphaProperties(),
      track = "alpha",
      rolloutFraction = 1000
    )

    assertThat(fake.trackUpdates.first().releaseNotes["en-US"]).hasLength(500)
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
