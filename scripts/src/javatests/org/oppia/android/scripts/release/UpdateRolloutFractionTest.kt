package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for [main] and [updateRollout] in the update_rollout_fraction script.
 *
 * [main] argument-validation tests cover all [require] and [requireNotNull] blocks without a
 * real Play Console connection. [updateRollout] tests exercise the integrated rollout-update
 * flow end-to-end using [FakePlayConsoleClient] and a temporary changelog directory.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UpdateRolloutFractionTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var fakeClient: FakePlayConsoleClient

  private val testPackageName = "org.oppia.android"
  private val testVersion = "0.18"

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
  }

  // ---------------------------------------------------------------------------
  // updateRollout() — live track validation
  // ---------------------------------------------------------------------------

  @Test
  fun testUpdateRollout_noLiveReleases_throwsIllegalStateException() {
    val exception = assertThrows<IllegalStateException> {
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
      )
    }

    assertThat(exception).hasMessageThat().contains("no live releases")
  }

  @Test
  fun testUpdateRollout_draftReleaseOnly_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "draft"))
    )

    val exception = assertThrows<IllegalStateException> {
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
      )
    }

    assertThat(exception).hasMessageThat().contains("no live releases")
  }

  @Test
  fun testUpdateRollout_liveReleasesWithNoVersionCodes_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = emptyList(), status = "inProgress"))
    )
    createSharedChangelog(testVersion, "Notes.")

    val exception = assertThrows<IllegalStateException> {
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
      )
    }

    assertThat(exception).hasMessageThat().contains("no version codes")
  }

  // ---------------------------------------------------------------------------
  // updateRollout() — rollout fraction update
  // ---------------------------------------------------------------------------

  @Test
  fun testUpdateRollout_inProgressRelease_updatesRolloutFraction() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(100L), status = "inProgress", rolloutFraction = 250
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates.single().rolloutFraction).isEqualTo(500)
  }

  @Test
  fun testUpdateRollout_completedRelease_updatesRolloutFraction() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "production", testVersion, 1000
    )

    assertThat(fakeClient.trackUpdates.single().rolloutFraction).isEqualTo(1000)
  }

  @Test
  fun testUpdateRollout_multipleVersionCodes_usesHighestVersionCode() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(98L, 100L, 99L), status = "inProgress", rolloutFraction = 100
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates.single().versionCode).isEqualTo(100L)
  }

  @Test
  fun testUpdateRollout_fullRolloutFraction_setsTo1000() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(200L), status = "inProgress", rolloutFraction = 500
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 1000
    )

    assertThat(fakeClient.trackUpdates.single().rolloutFraction).isEqualTo(1000)
  }

  @Test
  fun testUpdateRollout_rolloutFractionLessThanCurrent_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(100L), status = "inProgress", rolloutFraction = 500
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    val exception = assertThrows<IllegalStateException> {
      // 250 < 500 → rollout regression
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 250
      )
    }

    assertThat(exception).hasMessageThat().contains("can only increase")
  }

  @Test
  fun testUpdateRollout_rolloutFractionEqualToCurrent_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(200L), status = "inProgress", rolloutFraction = 500
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    val exception = assertThrows<IllegalStateException> {
      // 500 == 500 → not strictly greater, treated as a regression
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 500
      )
    }

    assertThat(exception).hasMessageThat().contains("can only increase")
  }

  // ---------------------------------------------------------------------------
  // updateRollout() — release notes preservation
  // ---------------------------------------------------------------------------

  @Test
  fun testUpdateRollout_withSharedChangelogFile_preservesNotesInUpdate() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"))
    )
    createSharedChangelog(testVersion, "Shared release notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Shared release notes.")
  }

  @Test
  fun testUpdateRollout_withTrackSpecificChangelogFile_usesTrackSpecificNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"))
    )
    createSharedChangelog(testVersion, "Shared notes.")
    createTrackChangelog(testVersion, "alpha", "Alpha-specific notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Alpha-specific notes.")
  }

  @Test
  fun testUpdateRollout_withNoChangelogFile_doesNotFailButPassesEmptyNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"))
    )
    // No changelog file created.
    File(tempFolder.root, "config/changelogs").mkdirs()

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes).isEmpty()
  }

  @Test
  fun testUpdateRollout_changelogExceedsMaxLength_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"))
    )
    createSharedChangelog(testVersion, "A".repeat(501))

    val exception = assertThrows<IllegalStateException> {
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
      )
    }

    assertThat(exception).hasMessageThat().contains("exceeds the 500 character limit")
  }

  // ---------------------------------------------------------------------------
  // updateRollout() — API call sequencing
  // ---------------------------------------------------------------------------

  @Test
  fun testUpdateRollout_createsEditThenSetsReleaseThenCommits() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"))
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.createdEdits).hasSize(1)
    assertThat(fakeClient.trackUpdates).hasSize(1)
    assertThat(fakeClient.committedEdits).hasSize(1)
    assertThat(fakeClient.committedEdits.single()).isEqualTo(fakeClient.createdEdits.single())
  }

  @Test
  fun testUpdateRollout_setsCorrectTrackAndPackageName() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "inProgress"))
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 750
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("beta")
    assertThat(update.packageName).isEqualTo(testPackageName)
  }

  // ---------------------------------------------------------------------------
  // main() — argument validation
  // ---------------------------------------------------------------------------

  @Test
  fun testMain_tooFewArguments_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(emptyArray())
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_tooManyArguments_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "pkg", "alpha", "0.17", "500", "token", "url", "extra"))
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_nonIntegerRolloutFraction_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "fifty", "token"))
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction must be an integer")
  }

  @Test
  fun testMain_blankWorkspacePath_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("", "org.oppia.android", "alpha", "0.17", "500", "token"))
    }

    assertThat(exception).hasMessageThat().contains("workspace_path must not be blank")
  }

  @Test
  fun testMain_blankPackageName_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "", "alpha", "0.17", "500", "token"))
    }

    assertThat(exception).hasMessageThat().contains("package_name must not be blank")
  }

  @Test
  fun testMain_invalidTrack_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "internal", "0.17", "500", "token"))
    }

    assertThat(exception).hasMessageThat().contains("track must be one of")
  }

  @Test
  fun testMain_invalidVersionFormat_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "invalid", "500", "token"))
    }

    assertThat(exception).hasMessageThat().contains("version must be in major.minor format")
  }

  @Test
  fun testMain_rolloutFractionBelowZero_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "-1", "token"))
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction must be between 0 and 1000")
  }

  @Test
  fun testMain_rolloutFractionAbove1000_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "1001", "token"))
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction must be between 0 and 1000")
  }

  @Test
  fun testMain_blankGcpAccessToken_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "500", ""))
    }

    assertThat(exception).hasMessageThat().contains("gcp_access_token must not be blank")
  }

  // ---------------------------------------------------------------------------
  // Frozen version code preservation
  // ---------------------------------------------------------------------------

  @Test
  fun testUpdateRollout_alphaTrack_preservesFrozenKitKatVersionCodeInTrackUpdate() {
    // vc 16 is permanently frozen on alpha; it must be re-included in every setTrackRelease call
    // so the Play Console API does not deactivate it when the rollout fraction is updated.
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(listOf(201L), "inProgress", rolloutFraction = 250),
        PlayConsoleClient.TrackRelease(listOf(16L), "completed")
      )
    )
    createSharedChangelog(testVersion, notes = "Release notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates).hasSize(1)
    assertThat(fakeClient.trackUpdates[0].versionCode).isEqualTo(201L)
    assertThat(fakeClient.trackUpdates[0].preservedVersionCodes).containsExactly(16L)
  }

  @Test
  fun testUpdateRollout_betaTrack_doesNotIncludeAnyFrozenVersionCodes() {
    // Beta has no frozen builds; the rollout update should not include any preserved version codes.
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(listOf(201L), "inProgress", rolloutFraction = 250))
    )
    createSharedChangelog(
      testVersion, notes = "Release notes."
    )

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates).hasSize(1)
    assertThat(fakeClient.trackUpdates[0].track).isEqualTo("beta")
    assertThat(fakeClient.trackUpdates[0].preservedVersionCodes).isEmpty()
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
