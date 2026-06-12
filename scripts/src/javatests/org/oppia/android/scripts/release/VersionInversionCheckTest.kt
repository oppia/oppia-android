package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [VersionInversionCheck]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class VersionInversionCheckTest {
  private lateinit var fakeClient: FakePlayConsoleClient
  private lateinit var check: VersionInversionCheck

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
    check = VersionInversionCheck(fakeClient)
  }

  // ---------------------------------------------------------------------------
  // Empty track (first-ever upload)
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_emptyTrack_anyVersionCode_passes() {
    // No existing releases → any version code is acceptable.
    check.verify("org.oppia.android", "alpha", newVersionCode = 1L)
  }

  @Test
  fun testVerify_emptyTrack_largeVersionCode_passes() {
    check.verify("org.oppia.android", "alpha", newVersionCode = 99_999L)
  }

  // ---------------------------------------------------------------------------
  // Single live release — strictly greater → pass
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_newVersionHigherThanLive_passes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )

    check.verify("org.oppia.android", "alpha", newVersionCode = 301L)
  }

  @Test
  fun testVerify_newVersionMuchHigherThanLive_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(100L)))
    )

    check.verify("org.oppia.android", "beta", newVersionCode = 500L)
  }

  // ---------------------------------------------------------------------------
  // Equal version code → fail (not strictly greater)
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_newVersionEqualToLive_throwsWithVersionCodes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "alpha", newVersionCode = 300L)
    }

    assertThat(exception).hasMessageThat().contains("Version inversion detected")
    assertThat(exception).hasMessageThat().contains("300")
  }

  // ---------------------------------------------------------------------------
  // Lower version code → fail
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_newVersionLowerThanLive_throwsWithVersionCodes() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(400L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "production", newVersionCode = 399L)
    }

    assertThat(exception).hasMessageThat().contains("Version inversion detected")
    assertThat(exception).hasMessageThat().contains("399")
    assertThat(exception).hasMessageThat().contains("400")
  }

  // ---------------------------------------------------------------------------
  // Multiple releases / multiple version codes — checks against highest
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_multipleReleases_newVersionHigherThanAll_passes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L, 250L)),
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L))
      )
    )

    check.verify("org.oppia.android", "alpha", newVersionCode = 301L)
  }

  @Test
  fun testVerify_multipleVersionCodesInRelease_newVersionLowerThanHighest_throws() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L, 400L))
      )
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "beta", newVersionCode = 350L)
    }

    // 350 < 400 — inversion relative to the highest code.
    assertThat(exception).hasMessageThat().contains("Version inversion detected")
    assertThat(exception).hasMessageThat().contains("400")
  }

  // ---------------------------------------------------------------------------
  // Track isolation
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_inversionOnDifferentTrack_doesNotAffectRequestedTrack() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(500L)))
    )
    // Production track is empty — version code 1 should still pass.
    check.verify("org.oppia.android", "production", newVersionCode = 1L)
  }

  // ---------------------------------------------------------------------------
  // Error message content
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_inversion_errorMentionsTrackName() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )

    val exception = assertThrows<IllegalStateException>() {
      check.verify("org.oppia.android", "beta", newVersionCode = 299L)
    }

    assertThat(exception).hasMessageThat().contains("beta")
  }
}
