package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
 * before any network call is made, so no mock server is needed for them. Full-flow tests call
 * [runMain], which injects the local [MockWebServer] URL as an optional 6th argument, routing all
 * Play Console API calls through the server. Individual precondition checkers are tested in their
 * own dedicated files: [VersionInversionCheckerTest], [PendingReleaseCheckerTest],
 * [ChangelogExistenceCheckerTest].
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UploadBinaryToPlayConsoleTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var server: MockWebServer

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
  }

  @After
  fun tearDown() {
    server.shutdown()
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
    enqueueSuccessfulUpload()

    // rollout_fraction = 0 is valid; the full upload flow should complete without errors.
    runMain(aab.absolutePath, rolloutFraction = 0)

    // Verify that commitEdit (the 9th and final request) was actually called, confirming the
    // upload was committed and not aborted early.
    skipRequests(8)
    assertThat(server.takeRequest().path).endsWith(":commit")
  }

  @Test
  fun testScript_rolloutFractionThousand_passesValidation() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    // rollout_fraction = 1000 (100%) is valid; the full upload flow should complete.
    runMain(aab.absolutePath, rolloutFraction = 1000)

    // Verify that commitEdit (the 9th and final request) was actually called, confirming the
    // upload was committed and not aborted early.
    skipRequests(8)
    assertThat(server.takeRequest().path).endsWith(":commit")
  }

  // ---------------------------------------------------------------------------
  // Full upload flow
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_noReleasesOnTrack_completesFullUploadFlow() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Bug fixes and performance improvements.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    // Verify the full 9-step flow completed by confirming commitEdit (step 9) was called.
    // Flow: 2 (pending check) + 2 (upload) + 3 (version check) + 1 (setTrackRelease) +
    // 1 (commitEdit). The :commit suffix on the path uniquely identifies the commit call.
    skipRequests(8)
    assertThat(server.takeRequest().path).endsWith(":commit")
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsAabFileBytes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    // Request 4 (index 3) is the uploadBundle call; skip the first 3 requests.
    // Flow: 2 (pending check: createEdit + getTrack) + 1 (createEdit upload) = 3.
    skipRequests(3)
    assertThat(server.takeRequest().bodySize).isEqualTo(64L)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_assignsReturnedVersionCodeToTrack() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload(versionCode = 301L)

    runMain(aab.absolutePath)

    // Flow: 2 (pending) + 2 (upload) + 3 (version check) = 7 previous requests.
    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("\"301\"")
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsChangelogAsReleaseNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "User-facing release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("User-facing release notes.")
    assertThat(setTrackBody).contains("en-US")
  }

  // ---------------------------------------------------------------------------
  // Precondition failures
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_pendingReleaseOnTrack_throwsBeforeUpload() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    val draftTrack =
      """{"releases":[{"versionCodes":["299"],"status":"draft"}]}"""
    enqueuePendingCheck(draftTrack)

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("Pending release detected")
    // Verify only the PendingReleaseChecker's 2 calls were made: the last request must be
    // GET .../tracks/alpha (the track-state read), not a bundle upload or commit.
    skipRequests(1)
    val lastRequest = server.takeRequest()
    assertThat(lastRequest.method).isEqualTo("GET")
    assertThat(lastRequest.path).contains("/tracks/alpha")
  }

  @Test
  fun testRunUpload_changelogFileMissing_throwsBeforeUpload() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    // No changelog file created.
    enqueuePendingCheck()

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("changelog")
    // Flow: 3 + 2 + 9 = 14 previous requests.
    skipRequests(1)
    val lastRequest = server.takeRequest()
    assertThat(lastRequest.method).isEqualTo("GET")
    assertThat(lastRequest.path).contains("/tracks/alpha")
  }

  @Test
  fun testRunUpload_crossTrackInversion_throwsAfterUploadNotCommitted() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    // Beta has vc=500; uploading vc=1 to alpha violates the alpha > beta constraint.
    val betaWithHighVc =
      """{"releases":[{"versionCodes":["500"],"status":"completed"}]}"""
    enqueuePendingCheck()
    enqueueUpload(versionCode = 1L)
    enqueueVersionCheck(betaBody = betaWithHighVc)

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("Version inversion")
    // Verify the last request was VersionInversionChecker's final GET .../tracks/production,
    // confirming setTrackRelease (PUT) and commitEdit were never called.
    // Flow: 2 (pending check) + 2 (upload) + 3 (version check) = 7 total; last is index 6.
    skipRequests(6)
    val lastGetRequest = server.takeRequest()
    assertThat(lastGetRequest.method).isEqualTo("GET")
    assertThat(lastGetRequest.path).contains("/tracks/production")
  }

  @Test
  fun testRunUpload_versionInversionChecker_reusesUploadEditIdForAllThreeTrackQueries() {
    // This test validates the core fix: VersionInversionChecker must reuse the upload edit session
    // rather than opening a new one. The Play Developer API only allows one active edit at a time,
    // so creating a temporary edit during the upload would invalidate the in-progress session.
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    // Request order: (0) pending-createEdit, (1) pending-getTrack, (2) upload-createEdit → "e-up",
    // (3) uploadBundle, (4) version-getTrack/alpha, (5) version-getTrack/beta,
    // (6) version-getTrack/production, (7) setTrackRelease, (8) commitEdit.
    skipRequests(4) // Skip to first VersionInversionChecker request.
    val alphaRequest = server.takeRequest()
    val betaRequest = server.takeRequest()
    val productionRequest = server.takeRequest()

    // All three track queries must use the upload edit ID "e-up", not a freshly created edit.
    assertThat(alphaRequest.path).contains("e-up")
    assertThat(betaRequest.path).contains("e-up")
    assertThat(productionRequest.path).contains("e-up")
    assertThat(alphaRequest.path).contains("/tracks/alpha")
    assertThat(betaRequest.path).contains("/tracks/beta")
    assertThat(productionRequest.path).contains("/tracks/production")
  }

  // ---------------------------------------------------------------------------
  // Rollout fraction forwarding
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_fullRollout_recordsStatusAsCompleted() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath, rolloutFraction = 1000)

    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("\"status\":\"completed\"")
    assertThat(setTrackBody).doesNotContain("userFraction")
  }

  @Test
  fun testRunUpload_stagedRollout_recordsStatusAsInProgressWithFraction() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath, rolloutFraction = 250)

    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("\"status\":\"inProgress\"")
    assertThat(setTrackBody).contains("0.25")
  }

  // ---------------------------------------------------------------------------
  // Changelog content behaviour
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_flavorSpecificChangelogExists_usesFlavorFileOverDefault() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Default notes.")
    createChangelog("0.17", flavor = "alpha", content = "Alpha-specific notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("Alpha-specific notes.")
    assertThat(setTrackBody).doesNotContain("Default notes.")
  }

  @Test
  fun testRunUpload_emptyChangelog_setsEmptyReleaseNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    // When the changelog is empty, no release-note entries are included.
    assertThat(setTrackBody).contains("\"releaseNotes\":[]")
  }

  @Test
  fun testRunUpload_changelogExceeds500Chars_throwsBeforeSetTrackRelease() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "a".repeat(600))
    // extractReleaseNotes runs after the version-inversion check; enqueue up to that point.
    enqueuePendingCheck()
    enqueueUpload()
    enqueueVersionCheck()

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("500")
    assertThat(exception).hasMessageThat().contains("600")
    // Verify the last request was VersionInversionChecker's final GET .../tracks/production,
    // confirming setTrackRelease (PUT) was never called.
    // Flow: 2 (pending check) + 2 (upload) + 3 (version check) = 7 total; last is index 6.
    skipRequests(6)
    val lastRequest = server.takeRequest()
    assertThat(lastRequest.method).isEqualTo("GET")
    assertThat(lastRequest.path).contains("/tracks/production")
  }

  @Test
  fun testRunUpload_changelogExactly500Chars_completesUploadWithFullNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "b".repeat(500))
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("b".repeat(500))
  }

  // ---------------------------------------------------------------------------
  // Frozen version code preservation (#6330)
  // ---------------------------------------------------------------------------

  @Test
  fun testRunUpload_alphaTrack_includesFrozenKitKatVersionCodeInTrackUpdateRequest() {
    // The alpha track has a permanently frozen KitKat build (vc 16) that must be re-included in
    // every setTrackRelease call. Without it the Play Console API would deactivate vc 16.
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload(versionCode = 202L)

    runMain(aab.absolutePath, track = "alpha")

    // Request 8 (index 7) is the PUT setTrackRelease for the alpha track.
    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("\"16\"")
    assertThat(setTrackBody).contains("\"202\"")
    // The frozen entry must be marked completed (no userFraction), while the new release has its
    // own status. Verify both version codes appear in the releases list.
    assertThat(setTrackBody).contains("\"releases\"")
  }

  @Test
  fun testRunUpload_betaTrack_doesNotIncludeAnyFrozenVersionCodesInTrackUpdateRequest() {
    // Beta currently has no frozen builds; the setTrackRelease body should only contain the new vc.
    val aab = createAab("oppia-android-0.17-rc01-beta-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload(versionCode = 202L)

    runMain(aab.absolutePath, track = "beta")

    // Request 8 (index 7) is the PUT setTrackRelease for the beta track.
    skipRequests(7)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("\"202\"")
    assertThat(setTrackBody).doesNotContain("\"16\"")
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
   * Calls [main] with a valid 5-argument set plus the mock server's base URL as a 6th argument,
   * so the script routes all Play Console API calls through [server].
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
        server.url("/").toString()
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

  /**
   * Enqueues the 2-response sequence for [PendingReleaseChecker]: a createEdit response followed
   * by a getTrack response with [trackBody].
   */
  private fun enqueuePendingCheck(
    trackBody: String =
      """{"releases":[]}"""
  ) {
    server.enqueue(MockResponse().setBody("""{"id":"e-pc"}""").setResponseCode(200))
    server.enqueue(MockResponse().setBody(trackBody).setResponseCode(200))
  }

  /**
   * Enqueues the 2-response sequence for the upload session: a createEdit response followed by an
   * uploadBundle response returning [versionCode].
   */
  private fun enqueueUpload(versionCode: Long = 1L) {
    server.enqueue(MockResponse().setBody("""{"id":"e-up"}""").setResponseCode(200))
    server.enqueue(
      MockResponse()
        .setBody("""{"versionCode":"$versionCode"}""")
        .setResponseCode(200)
    )
  }

  /**
   * Enqueues the 3-response sequence for [VersionInversionChecker]: 3 × getTrack for the alpha,
   * beta, and production tracks. No createEdit calls are made because the existing upload edit
   * ID is passed in, so only the GET requests are sent.
   */
  private fun enqueueVersionCheck(
    alphaBody: String =
      """{"releases":[]}""",
    betaBody: String =
      """{"releases":[]}""",
    productionBody: String =
      """{"releases":[]}"""
  ) {
    server.enqueue(MockResponse().setBody(alphaBody).setResponseCode(200))
    server.enqueue(MockResponse().setBody(betaBody).setResponseCode(200))
    server.enqueue(MockResponse().setBody(productionBody).setResponseCode(200))
  }

  /**
   * Enqueues all 9 responses for a successful end-to-end upload flow:
   * - [enqueuePendingCheck] (2)
   * - [enqueueUpload] (2)
   * - [enqueueVersionCheck] (3)
   * - setTrackRelease (1)
   * - commitEdit (1)
   */
  private fun enqueueSuccessfulUpload(
    versionCode: Long = 1L,
    pendingBody: String =
      """{"releases":[]}""",
    alphaBody: String =
      """{"releases":[]}""",
    betaBody: String =
      """{"releases":[]}""",
    productionBody: String =
      """{"releases":[]}"""
  ) {
    enqueuePendingCheck(pendingBody)
    enqueueUpload(versionCode)
    enqueueVersionCheck(alphaBody, betaBody, productionBody)
    // setTrackRelease: Retrofit parses the body as TrackResponse even if we don't use it.
    server.enqueue(MockResponse().setBody("""{"releases":[]}""").setResponseCode(200))
    // commitEdit: Retrofit parses the body as EditResponse.
    server.enqueue(MockResponse().setBody("""{"id":"e-committed"}""").setResponseCode(200))
  }

  /**
   * Discards the first [n] recorded requests from [server]. Used to navigate to a specific
   * request in the sequence when asserting on request bodies.
   */
  private fun skipRequests(n: Int) {
    repeat(n) { server.takeRequest() }
  }
}
