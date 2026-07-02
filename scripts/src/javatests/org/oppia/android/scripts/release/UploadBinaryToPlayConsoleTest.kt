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

    assertThat(server.requestCount).isEqualTo(12)
  }

  @Test
  fun testScript_rolloutFractionThousand_passesValidation() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    // rollout_fraction = 1000 (100%) is valid; the full upload flow should complete.
    runMain(aab.absolutePath, rolloutFraction = 1000)

    assertThat(server.requestCount).isEqualTo(12)
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

    // All 12 HTTP requests were made: 2 (pending check) + 2 (upload) +
    // 6 (version check) + 1 (setTrackRelease) + 1 (commitEdit).
    assertThat(server.requestCount).isEqualTo(12)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsAabFileBytes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    // Request 4 (index 3) is the uploadBundle call; skip the first 3 requests.
    skipRequests(3)
    assertThat(server.takeRequest().bodySize).isEqualTo(64L)
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_assignsReturnedVersionCodeToTrack() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "Release notes.")
    enqueueSuccessfulUpload(versionCode = 301L)

    runMain(aab.absolutePath)

    // Request 11 (index 10) is setTrackRelease; skip the first 10 requests.
    skipRequests(10)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("\"301\"")
  }

  @Test
  fun testRunUpload_noReleasesOnTrack_uploadsChangelogAsReleaseNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "User-facing release notes.")
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    skipRequests(10)
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
    // Only the PendingReleaseChecker's 2 HTTP calls were made; upload was not attempted.
    assertThat(server.requestCount).isEqualTo(2)
  }

  @Test
  fun testRunUpload_changelogFileMissing_throwsBeforeUpload() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    // No changelog file created.
    enqueuePendingCheck()

    val exception = assertThrows<IllegalStateException>() { runMain(aab.absolutePath) }

    assertThat(exception).hasMessageThat().contains("changelog")
    // ChangelogExistenceChecker throws before any upload HTTP call.
    assertThat(server.requestCount).isEqualTo(2)
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
    // 2 (pending check) + 2 (upload) + 6 (all three version-check tracks) = 10.
    // setTrackRelease and commitEdit were NOT called.
    assertThat(server.requestCount).isEqualTo(10)
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

    skipRequests(10)
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

    skipRequests(10)
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

    skipRequests(10)
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

    skipRequests(10)
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
    // Only 10 requests were made; setTrackRelease was NOT called.
    assertThat(server.requestCount).isEqualTo(10)
  }

  @Test
  fun testRunUpload_changelogExactly500Chars_completesUploadWithFullNotes() {
    val aab = createAab("oppia-android-0.17-rc01-alpha-e740815230.aab")
    createChangelog("0.17", content = "b".repeat(500))
    enqueueSuccessfulUpload()

    runMain(aab.absolutePath)

    skipRequests(10)
    val setTrackBody = server.takeRequest().body.readUtf8()
    assertThat(setTrackBody).contains("b".repeat(500))
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
   * Enqueues the 6-response sequence for [VersionInversionChecker]: 3 × (createEdit + getTrack)
   * for the alpha, beta, and production tracks in that order.
   */
  private fun enqueueVersionCheck(
    alphaBody: String =
      """{"releases":[]}""",
    betaBody: String =
      """{"releases":[]}""",
    productionBody: String =
      """{"releases":[]}"""
  ) {
    server.enqueue(MockResponse().setBody("""{"id":"e-vi-a"}""").setResponseCode(200))
    server.enqueue(MockResponse().setBody(alphaBody).setResponseCode(200))
    server.enqueue(MockResponse().setBody("""{"id":"e-vi-b"}""").setResponseCode(200))
    server.enqueue(MockResponse().setBody(betaBody).setResponseCode(200))
    server.enqueue(MockResponse().setBody("""{"id":"e-vi-p"}""").setResponseCode(200))
    server.enqueue(MockResponse().setBody(productionBody).setResponseCode(200))
  }

  /**
   * Enqueues all 12 responses for a successful end-to-end upload flow:
   * - [enqueuePendingCheck] (2)
   * - [enqueueUpload] (2)
   * - [enqueueVersionCheck] (6)
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
