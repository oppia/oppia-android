package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [PendingReleaseChecker]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class PendingReleaseCheckerTest {
  private lateinit var fakeClient: FakePlayConsoleClient
  private lateinit var check: PendingReleaseChecker

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
    check = PendingReleaseChecker(fakeClient)
  }

  // ---------------------------------------------------------------------------
  // Empty track (no releases at all)
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_emptyTrack_passes() {
    // No releases configured → check should pass.
    check.verify("org.oppia.android", "alpha")
  }

  // ---------------------------------------------------------------------------
  // Terminal statuses (completed / halted) → pass
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_completedRelease_passes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )

    check.verify("org.oppia.android", "alpha")
  }

  @Test
  fun testVerify_haltedRelease_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "halted", versionCodes = listOf(400L)))
    )

    check.verify("org.oppia.android", "beta")
  }

  @Test
  fun testVerify_multipleTerminalReleases_passes() {
    fakeClient.setTrackReleases(
      "production",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)),
        PlayConsoleClient.TrackRelease(status = "halted", versionCodes = listOf(299L))
      )
    )

    check.verify("org.oppia.android", "production")
  }

  // ---------------------------------------------------------------------------
  // Non-terminal statuses → fail
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_draftRelease_throwsWithStatus() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "draft", versionCodes = listOf(301L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("Pending release detected")
    assertThat(exception).hasMessageThat().contains("draft")
  }

  @Test
  fun testVerify_inProgressRelease_throwsWithStatus() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "inProgress", versionCodes = listOf(302L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "beta")
    }

    assertThat(exception).hasMessageThat().contains("inProgress")
  }

  @Test
  fun testVerify_pendingRelease_throwsWithVersionCodes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "draft", versionCodes = listOf(305L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("305")
  }

  // ---------------------------------------------------------------------------
  // Mixed statuses — one pending among terminal releases → fail
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_mixedReleases_onePending_throws() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)),
        PlayConsoleClient.TrackRelease(status = "inProgress", versionCodes = listOf(301L))
      )
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "beta")
    }

    assertThat(exception).hasMessageThat().contains("Pending release detected")
  }

  // ---------------------------------------------------------------------------
  // Track isolation — checking one track doesn't bleed into another
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_pendingOnDifferentTrack_doesNotAffectRequestedTrack() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "draft", versionCodes = listOf(300L)))
    )
    // Beta track is empty — should pass even though alpha has a pending release.
    check.verify("org.oppia.android", "beta")
  }
}
