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
    check.verify("org.oppia.android", "alpha", 1000)
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

    check.verify("org.oppia.android", "alpha", 1000)
  }

  @Test
  fun testVerify_haltedRelease_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "halted", versionCodes = listOf(400L)))
    )

    check.verify("org.oppia.android", "beta", 1000)
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

    check.verify("org.oppia.android", "production", 1000)
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
      check.verify("org.oppia.android", "alpha", 1000)
    }

    assertThat(exception).hasMessageThat().contains("Pending release detected")
    assertThat(exception).hasMessageThat().contains("draft")
  }

  @Test
  fun testVerify_inProgressRelease_newRolloutNotHigherThanCurrent_throwsWithStatus() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "inProgress", versionCodes = listOf(302L), rolloutFraction = 250
        )
      )
    )

    val exception = assertThrows<IllegalStateException>() {
      // 250 is not > 250 → should throw
      check.verify("org.oppia.android", "beta", 250)
    }

    assertThat(exception).hasMessageThat().contains("in-progress release")
  }

  @Test
  fun testVerify_inProgressRelease_newRolloutHigherThanCurrent_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "inProgress", versionCodes = listOf(302L), rolloutFraction = 250
        )
      )
    )

    // 500 > 250 → rollout increase, should pass.
    check.verify("org.oppia.android", "beta", 500)
  }

  @Test
  fun testVerify_inProgressRelease_fullRolloutAfterPartial_passes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "inProgress", versionCodes = listOf(100L), rolloutFraction = 100
        )
      )
    )

    // 1000 > 100 → increasing to full rollout, should pass.
    check.verify("org.oppia.android", "alpha", 1000)
  }

  @Test
  fun testVerify_inProgressRelease_lowerRolloutThanCurrent_throws() {
    fakeClient.setTrackReleases(
      "production",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "inProgress", versionCodes = listOf(500L), rolloutFraction = 500
        )
      )
    )

    val exception = assertThrows<IllegalStateException>() {
      // 250 < 500 → rollout decrease not allowed
      check.verify("org.oppia.android", "production", 250)
    }

    assertThat(exception).hasMessageThat().contains("rollout_fraction greater than 500")
  }

  @Test
  fun testVerify_pendingRelease_sameVersionCodesAsCurrent_throws() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "draft", versionCodes = listOf(305L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "alpha", 1000)
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
      // Existing inProgress has rolloutFraction = null (= 0); newRolloutFraction = 0 → not higher → throws.
      check.verify("org.oppia.android", "beta", 0)
    }

    assertThat(exception).hasMessageThat().contains("in-progress release already exists")
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
    check.verify("org.oppia.android", "beta", 1000)
  }
}
