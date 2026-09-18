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
 * Tests for [main] and [updateRollout] in the update_rollout_permille script.
 *
 * [main] argument-validation tests cover all [require] and [requireNotNull] blocks without a
 * real Play Console connection. [updateRollout] tests exercise the integrated rollout-update
 * flow end-to-end using [FakePlayConsoleClient] and a temporary changelog directory.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UpdateRolloutPermilleTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var fakeClient: FakePlayConsoleClient

  private val testFrozenVersionCodesPerTrack = mapOf(
    "alpha" to setOf(1000L),
    "beta" to setOf(2000L)
  )
  private val testAlphaFrozenBaseline =
    PlayConsoleClient.TrackRelease(
      versionCodes = testFrozenVersionCodesPerTrack.getValue("alpha").toList(), status = "completed"
    )
  private val testBetaFrozenBaseline =
    PlayConsoleClient.TrackRelease(
      versionCodes = testFrozenVersionCodesPerTrack.getValue("beta").toList(), status = "completed"
    )

  private val testPackageName = "org.oppia.android"
  private val testVersion = "0.18"

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
  }

  @After
  fun tearDown() {
    fakeClient.close()
  }

  @Test
  fun testUpdateRollout_noLiveReleases_throwsIllegalStateException() {
    val exception = assertThrows<IllegalStateException> {
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
        frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
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
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
        frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
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
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
        frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
      )
    }

    assertThat(exception).hasMessageThat().contains("no version codes")
  }

  @Test
  fun testUpdateRollout_inProgressRelease_updatesRolloutPermille() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(100L), status = "inProgress", rolloutPermille = 250
        ),
        testAlphaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    assertThat(fakeClient.trackUpdates.single().rolloutPermille).isEqualTo(500)
  }

  @Test
  fun testUpdateRollout_completedRelease_updatesRolloutPermille() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "completed"))
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "production", testVersion, 1000,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    assertThat(fakeClient.trackUpdates.single().rolloutPermille).isEqualTo(1000)
  }

  @Test
  fun testUpdateRollout_multipleVersionCodes_usesHighestVersionCode() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(98L, 100L, 99L), status = "inProgress", rolloutPermille = 100
        ),
        testAlphaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    assertThat(fakeClient.trackUpdates.single().versionCode).isEqualTo(100L)
  }

  @Test
  fun testUpdateRollout_fullRolloutPermille_setsTo1000() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(200L), status = "inProgress", rolloutPermille = 500
        ),
        testBetaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 1000,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    assertThat(fakeClient.trackUpdates.single().rolloutPermille).isEqualTo(1000)
  }

  @Test
  fun testUpdateRollout_rolloutPermilleLessThanCurrent_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(100L), status = "inProgress", rolloutPermille = 500
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    val exception = assertThrows<IllegalStateException> {
      // 250 < 500 → rollout regression
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 250,
        frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
      )
    }

    assertThat(exception).hasMessageThat().contains("can only increase")
  }

  @Test
  fun testUpdateRollout_rolloutPermilleEqualToCurrent_throwsIllegalStateException() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          versionCodes = listOf(200L), status = "inProgress", rolloutPermille = 500
        )
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    val exception = assertThrows<IllegalStateException> {
      // 500 == 500 → not strictly greater, treated as a regression
      updateRollout(
        fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 500,
        frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
      )
    }

    assertThat(exception).hasMessageThat().contains("can only increase")
  }

  @Test
  fun testUpdateRollout_withSharedChangelogFile_preservesNotesInUpdate() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"),
        testAlphaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Shared release notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Shared release notes.")
  }

  @Test
  fun testUpdateRollout_withTrackSpecificChangelogFile_usesTrackSpecificNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"),
        testAlphaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Shared notes.")
    createTrackChangelog(testVersion, "alpha", "Alpha-specific notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    assertThat(fakeClient.trackUpdates.single().releaseNotes)
      .containsEntry("en-US", "Alpha-specific notes.")
  }

  @Test
  fun testUpdateRollout_withNoChangelogFile_succeedsWithEmptyNotes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"),
        testAlphaFrozenBaseline
      )
    )
    // No changelog file created.
    File(tempFolder.root, "config/changelogs").mkdirs()

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
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
        fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
        frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
      )
    }

    assertThat(exception).hasMessageThat().contains("exceeds the 500 character limit")
  }

  @Test
  fun testUpdateRollout_createsEdit_thenSetsRelease_thenCommits() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "inProgress"),
        testAlphaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
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
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "inProgress"),
        testBetaFrozenBaseline
      )
    )
    createSharedChangelog(testVersion, "Notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 750,
      frozenVersionCodesPerTrack = testFrozenVersionCodesPerTrack
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("beta")
    assertThat(update.packageName).isEqualTo(testPackageName)
  }

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
  fun testMain_nonIntegerRolloutPermille_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "fifty", "token"))
    }

    assertThat(exception).hasMessageThat().contains("rollout_permille must be an integer")
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
  fun testMain_rolloutPermilleBelowZero_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "-1", "token"))
    }

    assertThat(exception).hasMessageThat().contains("rollout_permille must be between 0 and 1000")
  }

  @Test
  fun testMain_rolloutPermilleAbove1000_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "1001", "token"))
    }

    assertThat(exception).hasMessageThat().contains("rollout_permille must be between 0 and 1000")
  }

  @Test
  fun testMain_blankGcpAccessToken_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/ws", "org.oppia.android", "alpha", "0.17", "500", ""))
    }

    assertThat(exception).hasMessageThat().contains("gcp_access_token must not be blank")
  }

  @Test
  fun testUpdateRollout_alphaTrack_preservesFrozenVersionCodesInTrackUpdate() {
    // All frozen alpha version codes (defined in FrozenReleaseConfig) must be merged into every
    // setTrackRelease call so the Play Console API does not deactivate them.
    val liveVersionCode = (FROZEN_VERSION_CODES_PER_TRACK["alpha"]?.maxOrNull() ?: 0L) + 1
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          listOf(liveVersionCode), "inProgress", rolloutPermille = 250
        ),
        FROZEN_ALPHA_BASELINE
      )
    )
    createSharedChangelog(testVersion, notes = "Release notes.")

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "alpha", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates).hasSize(1)
    assertThat(fakeClient.trackUpdates[0].versionCode).isEqualTo(liveVersionCode)
    assertThat(fakeClient.trackUpdates[0].rolloutPermille).isEqualTo(500)
    // Assertions derive from FROZEN_VERSION_CODES_PER_TRACK so they stay correct when
    // FrozenReleaseConfig is updated without requiring manual test changes.
    assertThat(fakeClient.trackUpdates[0].frozenVersionCodes)
      .containsExactlyElementsIn(FROZEN_VERSION_CODES_PER_TRACK["alpha"] ?: emptySet<Long>())
  }

  @Test
  fun testUpdateRollout_betaTrack_frozenVersionCodesMatchFrozenConfig() {
    // Beta frozen codes (from FrozenReleaseConfig) must be present in the track update. The
    // assertion derives directly from FROZEN_VERSION_CODES_PER_TRACK so it stays resilient when
    // codes are added to or removed from the config.
    val liveVersionCode = (FROZEN_VERSION_CODES_PER_TRACK["beta"]?.maxOrNull() ?: 0L) + 1
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          listOf(liveVersionCode), "inProgress", rolloutPermille = 250
        ),
        FROZEN_BETA_BASELINE
      )
    )
    createSharedChangelog(
      testVersion, notes = "Release notes."
    )

    updateRollout(
      fakeClient, tempFolder.root.absolutePath, testPackageName, "beta", testVersion, 500
    )

    assertThat(fakeClient.trackUpdates).hasSize(1)
    assertThat(fakeClient.trackUpdates[0].track).isEqualTo("beta")
    assertThat(fakeClient.trackUpdates[0].versionCode).isEqualTo(liveVersionCode)
    assertThat(fakeClient.trackUpdates[0].rolloutPermille).isEqualTo(500)
    assertThat(fakeClient.trackUpdates[0].frozenVersionCodes)
      .containsExactlyElementsIn(FROZEN_VERSION_CODES_PER_TRACK["beta"] ?: emptySet<Long>())
  }

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
