package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for [main] and [maybeUploadUpdatedChangelogs] in the upload_changelog_to_play_console
 * script.
 *
 * [main] argument-validation tests cover the [require] blocks without a real Play Console
 * connection. [maybeUploadUpdatedChangelogs] tests exercise the integrated changelog-upload
 * flow end-to-end using [FakePlayConsoleClient] and a temporary changelog directory.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UploadChangelogToPlayConsoleTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var fakeClient: FakePlayConsoleClient

  private val testPackageName = "org.oppia.android"
  private val testVersion = "0.18"
  private val testNotes = "Release notes."

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
  }

  // ---------------------------------------------------------------------------
  // maybeUploadUpdatedChangelogs() — live track detection
  // ---------------------------------------------------------------------------

  @Test
  fun testMaybeUploadUpdatedChangelogs_noLiveTracks_doesNotCreateAnyEdits() {
    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.createdEdits).isEmpty()
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_draftTrackOnly_doesNotCreateAnyEdits() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "draft"))
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.createdEdits).isEmpty()
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_haltedTrackOnly_doesNotCreateAnyEdits() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "halted"))
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.createdEdits).isEmpty()
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_completedAlphaTrack_uploadsCorrectNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("alpha")
    assertThat(update.versionCode).isEqualTo(100L)
    assertThat(update.releaseNotes).containsEntry("en-US", testNotes)
    assertThat(fakeClient.committedEdits).hasSize(1)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_inProgressBetaTrack_uploadsCorrectNotesAndFraction() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(200L), status = "inProgress", rolloutFraction = 250
        )
      )
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("beta")
    assertThat(update.versionCode).isEqualTo(200L)
    assertThat(update.rolloutFraction).isEqualTo(250)
    assertThat(update.releaseNotes).containsEntry("en-US", testNotes)
    assertThat(fakeClient.committedEdits).hasSize(1)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_completedProductionTrack_uploadsCorrectNotes() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "completed"))
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("production")
    assertThat(update.versionCode).isEqualTo(300L)
    assertThat(update.releaseNotes).containsEntry("en-US", testNotes)
    assertThat(fakeClient.committedEdits).hasSize(1)
  }

  // ---------------------------------------------------------------------------
  // maybeUploadUpdatedChangelogs() — changelog file resolution
  // ---------------------------------------------------------------------------

  @Test
  fun testMaybeUploadUpdatedChangelogs_liveTrackWithSharedChangelogOnly_usesSharedNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Shared notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Shared notes.")
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_liveTrackWithTrackSpecificChangelog_usesTrackSpecificNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Shared notes.")
    createTrackChangelog(testVersion, "alpha", "Alpha-specific notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Alpha-specific notes.")
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_liveTrackWithNoMatchingChangelogFile_skipsUpload() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    // No changelog file created for testVersion — only the dir exists.
    File(tempFolder.root, "config/changelogs").mkdirs()

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.createdEdits).isEmpty()
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_trackSpecificFileOnlyPresent_usesTrackSpecificNotes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "completed"))
    )
    // No shared file — only the beta-specific one.
    createTrackChangelog(testVersion, "beta", "Beta-specific notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Beta-specific notes.")
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_changelogWithTrailingWhitespace_uploadsTrimmedNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "  Release notes.  \n")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Release notes.")
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_changelogExceedsMaxLength_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "A".repeat(501))

    val exception = assertThrows<IllegalStateException> {
      maybeUploadUpdatedChangelogs(
        fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
      )
    }

    assertThat(exception).hasMessageThat().contains("exceeds the 500 character limit")
  }

  // ---------------------------------------------------------------------------
  // maybeUploadUpdatedChangelogs() — multiple tracks
  // ---------------------------------------------------------------------------

  @Test
  fun testMaybeUploadUpdatedChangelogs_multipleLiveTracks_uploadsCorrectNotesToAll() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "completed"))
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.committedEdits).hasSize(2)
    assertThat(fakeClient.trackUpdates).hasSize(2)
    assertThat(fakeClient.trackUpdates.map { it.releaseNotes["en-US"] })
      .containsExactly(testNotes, testNotes)
    assertThat(fakeClient.trackUpdates.map { it.versionCode })
      .containsExactly(100L, 200L)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_oneTrackHasNoFile_uploadsOnlyToTrackWithFile() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "completed"))
    )
    // Only alpha-specific file — no shared file for beta to fall back on.
    createTrackChangelog(testVersion, "alpha", "Alpha notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.committedEdits).hasSize(1)
    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("alpha")
    assertThat(update.releaseNotes).containsEntry("en-US", "Alpha notes.")
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_allThreeTracksLive_uploadsCorrectNotesToAll() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "completed"))
    )
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "completed"))
    )
    createSharedChangelog(testVersion, testNotes)

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.committedEdits).hasSize(3)
    assertThat(fakeClient.trackUpdates).hasSize(3)
    assertThat(fakeClient.trackUpdates.map { it.releaseNotes["en-US"] })
      .containsExactly(testNotes, testNotes, testNotes)
  }

  // ---------------------------------------------------------------------------
  // maybeUploadUpdatedChangelogs() — version code selection
  // ---------------------------------------------------------------------------

  @Test
  fun testMaybeUploadUpdatedChangelogs_singleVersionCode_usesItForUpdate() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().versionCode).isEqualTo(100L)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_multipleVersionCodes_usesHighestVersionCode() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(98L, 100L, 99L), status = "completed")
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().versionCode).isEqualTo(100L)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_noVersionCodes_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = emptyList(), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    val exception = assertThrows<IllegalStateException> {
      maybeUploadUpdatedChangelogs(
        fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
      )
    }

    assertThat(exception).hasMessageThat().contains("no version codes")
  }

  // ---------------------------------------------------------------------------
  // maybeUploadUpdatedChangelogs() — rollout fraction preservation
  // ---------------------------------------------------------------------------

  @Test
  fun testMaybeUploadUpdatedChangelogs_completedRelease_usesFullRolloutFraction() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().rolloutFraction).isEqualTo(1000)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_inProgressWithPartialRollout_preservesExistingFraction() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(100L), status = "inProgress", rolloutFraction = 250
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().rolloutFraction).isEqualTo(250)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_inProgressWithFullRollout_preservesFullFraction() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(100L), status = "inProgress", rolloutFraction = 1000
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.trackUpdates.single().rolloutFraction).isEqualTo(1000)
  }

  // ---------------------------------------------------------------------------
  // maybeUploadUpdatedChangelogs() — API call sequencing
  // ---------------------------------------------------------------------------

  @Test
  fun testMaybeUploadUpdatedChangelogs_singleLiveTrack_createsEditBeforeSettingNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.createdEdits).hasSize(1)
    assertThat(fakeClient.trackUpdates).hasSize(1)
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_singleLiveTrack_commitsEditAfterSettingNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    assertThat(fakeClient.committedEdits).hasSize(1)
    assertThat(fakeClient.committedEdits.single()).isEqualTo(fakeClient.createdEdits.single())
  }

  @Test
  fun testMaybeUploadUpdatedChangelogs_singleLiveTrack_setsCorrectTrackAndPackageName() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    maybeUploadUpdatedChangelogs(
      fakeClient, tempFolder.root.absolutePath, testPackageName, testVersion
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("production")
    assertThat(update.packageName).isEqualTo(testPackageName)
  }

  // ---------------------------------------------------------------------------
  // main() — argument validation
  // ---------------------------------------------------------------------------

  @Test
  fun testMain_wrongArgumentCount_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(emptyArray())
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_tooManyArguments_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "0.17", "token", "url", "extra"))
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_blankWorkspacePath_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("", "org.oppia.android", "0.17", "token"))
    }

    assertThat(exception).hasMessageThat().contains("workspace_path must not be blank")
  }

  @Test
  fun testMain_blankPackageName_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "", "0.17", "token"))
    }

    assertThat(exception).hasMessageThat().contains("package_name must not be blank")
  }

  @Test
  fun testMain_blankGcpAccessToken_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "0.17", ""))
    }

    assertThat(exception).hasMessageThat().contains("gcp_access_token must not be blank")
  }

  @Test
  fun testMain_invalidVersionFormat_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "invalid-version", "token"))
    }

    assertThat(exception).hasMessageThat().contains("version must be in major.minor format")
  }

  @Test
  fun testMain_versionWithoutMinor_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "17", "token"))
    }

    assertThat(exception).hasMessageThat().contains("version must be in major.minor format")
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Creates `config/changelogs/<version>.md` in the temp folder with [notes]. */
  private fun createSharedChangelog(version: String, notes: String) {
    changelogsDir().resolve("$version.md").writeText(notes)
  }

  /** Creates `config/changelogs/<version>_<track>.md` in the temp folder with [notes]. */
  private fun createTrackChangelog(version: String, track: String, notes: String) {
    changelogsDir().resolve("${version}_$track.md").writeText(notes)
  }

  private fun changelogsDir(): File =
    File(tempFolder.root, "config/changelogs").also { it.mkdirs() }
}
