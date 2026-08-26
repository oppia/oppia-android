package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for the upload_binary_to_play_console script.
 *
 * Argument-validation tests call [runScript] directly with deliberately invalid inputs; they throw
 * before any network call is made, so no fake is needed for them. Full-flow tests call [runMain],
 * which injects [FakePlayConsoleClient.serverUrl] as a 6th argument, routing all Play Console API
 * calls through the fake's embedded server. The fake records result-level state (committed edits,
 * uploaded version codes, track update bodies, etc.) so tests can make typed assertions without
 * counting or ordering raw HTTP requests.
 *
 * Individual precondition checkers are tested in their own dedicated files:
 * [VersionInversionCheckerTest], [PendingReleaseCheckerTest], [ChangelogExistenceCheckerTest].
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UploadBinaryToPlayConsoleTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var fake: FakePlayConsoleClient

  @Before
  fun setUp() {
    fake = FakePlayConsoleClient()
    // Pre-configure alpha and beta with their frozen-code baselines so every test satisfies
    // the production invariant check. Tests that need a different track state override via
    // fake.setTrackReleases(...) afterwards, including FROZEN_ALPHA_BASELINE / FROZEN_BETA_BASELINE
    // alongside any test-specific releases.
    fake.setUpFrozenBaselines()
  }

  @After
  fun tearDown() {
    fake.close()
  }

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
    // 7 args (> 6) should fail; the 6th arg is the internal API base-URL override.
    val exception = assertThrows<IllegalArgumentException>() {
      runScript("a", "b", "c", "d", "e", "extra", "tooMany")
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
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    // rollout_fraction = 0 is valid; the full upload flow should complete without errors.
    runMain(aab.absolutePath, rolloutFraction = 0)

    // Verify commitEdit (the final step) was actually called, confirming the upload was not aborted.
    assertThat(fake.committedEdits).hasSize(1)
  }

  @Test
  fun testScript_rolloutFractionThousand_passesValidation() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    // rollout_fraction = 1000 (100%) is valid; the full upload flow should complete.
    runMain(aab.absolutePath, rolloutFraction = 1000)

    assertThat(fake.committedEdits).hasSize(1)
  }

  // ---------------------------------------------------------------------------
  // Full upload flow
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_noReleasesOnTrack_completesFullUploadFlow() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Bug fixes and performance improvements.")

    runMain(aab.absolutePath)

    // The final step of the upload flow is commitEdit; its presence confirms the entire
    // flow completed successfully.
    assertThat(fake.committedEdits).hasSize(1)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsAabFileBytes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runMain(aab.absolutePath)

    // Verify the AAB body size reached the server, confirming real bytes were transmitted.
    assertThat(fake.lastUploadedBodySize).isEqualTo(64L)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_assignsReturnedVersionCodeToTrack() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    fake.setNextVersionCode(301L)

    runMain(aab.absolutePath)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("\"301\"")
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsChangelogAsReleaseNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "User-facing release notes.")

    runMain(aab.absolutePath)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("User-facing release notes.")
    assertThat(fake.trackUpdateBodies.first()).contains("en-US")
  }

  // ---------------------------------------------------------------------------
  // Precondition failures
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_pendingReleaseOnTrack_throwsBeforeUpload() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    fake.setTrackReleases(
      "alpha",
      listOf(FROZEN_ALPHA_BASELINE, PlayConsoleClient.TrackRelease(listOf(299L), "draft"))
    )

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("Pending release detected")
    // No bundle was uploaded and no edit was committed, confirming the abort was clean.
    assertThat(fake.uploadedVersionCodes).isEmpty()
    assertThat(fake.committedEdits).isEmpty()
  }

  @Test
  fun testRunUpload_changelogFileMissing_throwsBeforeUpload() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    // No changelog file created.

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("changelog")
    assertThat(fake.uploadedVersionCodes).isEmpty()
    assertThat(fake.committedEdits).isEmpty()
  }

  @Test
  fun testRunUpload_crossTrackInversion_throwsAfterUploadNotCommitted() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    // Beta has vc=500; uploading vc=1 to alpha violates the alpha > beta constraint.
    fake.setTrackReleases(
      "beta",
      listOf(FROZEN_BETA_BASELINE, PlayConsoleClient.TrackRelease(listOf(500L), "completed"))
    )

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("Version inversion")
    // The upload happened but the edit was never committed.
    assertThat(fake.uploadedVersionCodes).hasSize(1)
    assertThat(fake.committedEdits).isEmpty()
  }

  @Test
  fun testRunUpload_versionInversionChecker_reusesUploadEditIdForAllThreeTrackQueries() {
    // This test validates the core fix: VersionInversionChecker must reuse the upload edit session
    // rather than opening a new one for each track query. The Play Developer API only allows one
    // active edit at a time, so creating a temporary edit during the upload would invalidate the
    // in-progress session.
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runMain(aab.absolutePath)

    // Five GET /tracks/ calls in total: one from PendingReleaseChecker (using a temp edit),
    // three from VersionInversionChecker (all reusing the upload edit ID), and one more from
    // the frozen-release lookup (also reusing the upload edit ID).
    assertThat(fake.trackQueriedEditIds).hasSize(5)
    val pendingCheckEditId = fake.trackQueriedEditIds[0]
    val uploadEditId = fake.trackQueriedEditIds[1]
    // The upload edit is a fresh session distinct from the pending-check temp edit.
    assertThat(uploadEditId).isNotEqualTo(pendingCheckEditId)
    // VersionInversionChecker and the frozen-release lookup must all reuse the upload edit ID.
    assertThat(fake.trackQueriedEditIds.drop(1))
      .containsExactly(uploadEditId, uploadEditId, uploadEditId, uploadEditId)
      .inOrder()
  }

  // ---------------------------------------------------------------------------
  // Rollout fraction forwarding
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_fullRollout_recordsStatusAsCompleted() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runMain(aab.absolutePath, rolloutFraction = 1000)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("\"status\":\"completed\"")
    assertThat(fake.trackUpdateBodies.first()).doesNotContain("userFraction")
  }

  @Test
  fun testRunUpload_stagedRollout_recordsStatusAsInProgressWithFraction() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runMain(aab.absolutePath, rolloutFraction = 250)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("\"status\":\"inProgress\"")
    assertThat(fake.trackUpdateBodies.first()).contains("0.25")
  }

  // ---------------------------------------------------------------------------
  // Changelog content behaviour
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_flavorSpecificChangelogExists_usesFlavorFileOverDefault() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Default notes.")
    createChangelog("0.17", flavor = "alpha", content = "Alpha-specific notes.")

    runMain(aab.absolutePath)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("Alpha-specific notes.")
    assertThat(fake.trackUpdateBodies.first()).doesNotContain("Default notes.")
  }

  @Test
  fun testRunUpload_emptyChangelog_setsEmptyReleaseNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "")

    runMain(aab.absolutePath)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    // When the changelog is empty, no release-note entries are included.
    assertThat(fake.trackUpdateBodies.first()).contains("\"releaseNotes\":[]")
  }

  @Test
  fun testRunUpload_changelogExceeds500Chars_throwsBeforeSetTrackRelease() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "a".repeat(600))

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("500")
    assertThat(exception).hasMessageThat().contains("600")
    // setTrackRelease and commitEdit must not have been called.
    assertThat(fake.trackUpdateBodies).isEmpty()
    assertThat(fake.committedEdits).isEmpty()
  }

  @Test
  fun testRunUpload_changelogExactly500Chars_completesUploadWithFullNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "b".repeat(500))

    runMain(aab.absolutePath)

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("b".repeat(500))
  }

  // ---------------------------------------------------------------------------
  // Frozen version code preservation
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_alphaTrack_includesFrozenKitKatVersionCodeInTrackUpdateRequest() {
    // The alpha track has a permanently frozen KitKat build (vc 16) that must be merged into
    // every new release entry. Without it the Play Console API would deactivate vc 16.
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    // Configure the alpha track to return vc 16 when the frozen-release GET is issued, so
    // the production code picks it up and passes it through unmodified to setTrackRelease.
    fake.setTrackReleases(
      "alpha",
      listOf(FROZEN_ALPHA_BASELINE)
    )
    fake.setNextVersionCode(202L)

    runMain(aab.absolutePath, track = "alpha")

    val setTrackBody = fake.trackUpdateBodies.first()
    assertThat(setTrackBody).contains("\"16\"")
    assertThat(setTrackBody).contains("\"201\"")
    assertThat(setTrackBody).contains("\"202\"")
    // All version codes are merged into a SINGLE release entry (not separate ones).
    assertThat(setTrackBody.split("\"versionCodes\"").size - 1).isEqualTo(1)
    assertThat(setTrackBody).contains("\"completed\"")
  }

  @Test
  fun testRunUpload_alphaTrack_includesAllFrozenVersionCodesWhenMultipleFrozenBuildsExist() {
    // Regression: when a second build is added to the frozen map (e.g. a future Lollipop freeze),
    // both frozen version codes must be merged into the new release entry alongside the new upload.
    // Uses FakePlayConsoleClient with an injected two-entry frozen map to avoid HTTP mock overhead.
    val fakeClient = FakePlayConsoleClient()
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(listOf(16L), "completed"),
        PlayConsoleClient.TrackRelease(listOf(21L), "completed")
      )
    )
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")

    runUpload(
      client = fakeClient,
      workspaceRoot = tempFolder.root.absolutePath,
      aabPath = aab.absolutePath,
      properties = parseAabFilename(aab.name)!!,
      track = "alpha",
      rolloutFraction = 10,
      frozenVersionCodesPerTrack = mapOf("alpha" to setOf(16L, 21L))
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.frozenVersionCodes).containsExactly(16L, 21L)
  }

  @Test
  fun testRunUpload_betaTrack_includesFrozenBetaVersionCodeInTrackUpdateRequest() {
    // Beta has a frozen build (vc 196) that must be merged into the track update so the Play
    // Console API does not deactivate it. Alpha's frozen codes must NOT appear in beta's update.
    val aab = createAab("oppia-android-0.17-rc01-beta-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    // Bump alpha to vc 300 so that uploading vc 202 to beta doesn't violate the version-inversion
    // check (which requires alpha vc > beta vc).
    fake.setTrackReleases(
      "alpha",
      listOf(FROZEN_ALPHA_BASELINE, PlayConsoleClient.TrackRelease(listOf(300L), "completed"))
    )
    fake.setNextVersionCode(202L)

    runMain(aab.absolutePath, track = "beta")

    assertThat(fake.trackUpdateBodies).hasSize(1)
    assertThat(fake.trackUpdateBodies.first()).contains("\"202\"")
    assertThat(fake.trackUpdateBodies.first()).contains("\"196\"")
    // Alpha-specific frozen codes must not bleed into the beta track update.
    assertThat(fake.trackUpdateBodies.first()).doesNotContain("\"16\"")
    assertThat(fake.trackUpdateBodies.first()).doesNotContain("\"201\"")
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Calls [main] with the 5 standard positional arguments. Used for validation tests that throw
   * before any Play Console HTTP calls are made.
   */
  private fun runScript(vararg args: String) {
    main(args.toList().toTypedArray())
  }

  /**
   * Calls [main] with a valid 5-argument set plus [FakePlayConsoleClient.serverUrl] as a 6th
   * argument, so the script routes all Play Console API calls through [fake]'s embedded server.
   */
  private fun runMain(
    aabPath: String,
    track: String = "alpha",
    rolloutFraction: Int = 1000
  ) {
    main(
      arrayOf(
        tempFolder.root.absolutePath,
        aabPath,
        track,
        "test-token",
        rolloutFraction.toString(),
        fake.serverUrl
      )
    )
  }

  /** Creates a dummy [ByteArray] AAB file in the temp folder with the given [name]. */
  private fun createAab(name: String): File =
    tempFolder.newFile(name).also { it.writeBytes(ByteArray(64)) }

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
