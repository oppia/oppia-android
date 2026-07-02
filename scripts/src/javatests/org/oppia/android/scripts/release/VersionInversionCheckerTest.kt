package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [VersionInversionChecker]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class VersionInversionCheckerTest {
  private lateinit var fakeClient: FakePlayConsoleClient
  private lateinit var checker: VersionInversionChecker

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
    checker = VersionInversionChecker(fakeClient)
  }

  // ---------------------------------------------------------------------------
  // Deploying to ALPHA — must be greater than beta and ga
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_alpha_allTracksEmpty_passes() {
    // No existing releases on any track — any version code is valid.
    checker.verify("org.oppia.android", "alpha", newVersionCode = 1L)
  }

  @Test
  fun testVerify_alpha_greaterThanBeta_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    checker.verify("org.oppia.android", "alpha", newVersionCode = 201L)
  }

  @Test
  fun testVerify_alpha_greaterThanBetaAndGa_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(100L)))
    )
    checker.verify("org.oppia.android", "alpha", newVersionCode = 201L)
  }

  @Test
  fun testVerify_alpha_lowerThanBeta_throwsMentionsBeta() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "alpha", newVersionCode = 299L)
    }
    assertThat(exception).hasMessageThat().contains("Version inversion")
    assertThat(exception).hasMessageThat().contains("beta")
    assertThat(exception).hasMessageThat().contains("300")
  }

  @Test
  fun testVerify_alpha_lowerThanGa_throwsMentionsGa() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(500L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "alpha", newVersionCode = 499L)
    }
    assertThat(exception).hasMessageThat().contains("ga")
    assertThat(exception).hasMessageThat().contains("500")
  }

  // ---------------------------------------------------------------------------
  // Deploying to BETA — must be greater than ga and less than alpha
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_beta_allTracksEmpty_passes() {
    checker.verify("org.oppia.android", "beta", newVersionCode = 100L)
  }

  @Test
  fun testVerify_beta_betweenGaAndAlpha_passes() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(100L)))
    )
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )
    checker.verify("org.oppia.android", "beta", newVersionCode = 200L)
  }

  @Test
  fun testVerify_beta_lowerThanGa_throws() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "beta", newVersionCode = 100L)
    }
    assertThat(exception).hasMessageThat().contains("ga")
    assertThat(exception).hasMessageThat().contains("200")
  }

  @Test
  fun testVerify_beta_greaterThanAlpha_throws() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "beta", newVersionCode = 300L)
    }
    assertThat(exception).hasMessageThat().contains("alpha")
    assertThat(exception).hasMessageThat().contains("200")
    assertThat(exception).hasMessageThat().contains("all alpha version codes")
  }

  // ---------------------------------------------------------------------------
  // Deploying to GA (production) — must be less than beta and alpha
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_ga_allTracksEmpty_passes() {
    checker.verify("org.oppia.android", "production", newVersionCode = 100L)
  }

  @Test
  fun testVerify_ga_lessThanBetaAndAlpha_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )
    checker.verify("org.oppia.android", "production", newVersionCode = 100L)
  }

  @Test
  fun testVerify_ga_greaterThanBeta_throws() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "production", newVersionCode = 300L)
    }
    assertThat(exception).hasMessageThat().contains("beta")
    assertThat(exception).hasMessageThat().contains("200")
    assertThat(exception).hasMessageThat().contains("all beta version codes")
  }

  @Test
  fun testVerify_ga_greaterThanAlpha_throws() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "production", newVersionCode = 400L)
    }
    assertThat(exception).hasMessageThat().contains("alpha")
    assertThat(exception).hasMessageThat().contains("300")
    assertThat(exception).hasMessageThat().contains("all alpha version codes")
  }

  // ---------------------------------------------------------------------------
  // Multiple releases / multiple version codes — "greater than" checks use max,
  // "less than" checks use min (strictest bound in each direction)
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_alpha_multipleVersionCodesOnBeta_checksHighest() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(100L, 200L))
      )
    )
    // 201 > 200 (max beta) → passes
    checker.verify("org.oppia.android", "alpha", newVersionCode = 201L)
  }

  @Test
  fun testVerify_alpha_multipleVersionCodesOnBeta_lowerThanHighest_throws() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(100L, 400L))
      )
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "alpha", newVersionCode = 300L)
    }
    assertThat(exception).hasMessageThat().contains("400")
  }

  @Test
  fun testVerify_beta_multipleVersionCodesOnAlpha_checksLowest() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(305L, 310L))
      )
    )
    // 304 < 305 (min alpha) → passes
    checker.verify("org.oppia.android", "beta", newVersionCode = 304L)
  }

  @Test
  fun testVerify_beta_betweenAlphaVersionCodes_throws() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(305L, 310L))
      )
    )
    // 307 lies between alpha's 305 and 310 — must fail (307 > min alpha = 305).
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "beta", newVersionCode = 307L)
    }
    assertThat(exception).hasMessageThat().contains("305")
  }

  @Test
  fun testVerify_ga_multipleVersionCodesOnBeta_checksLowest() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L, 210L))
      )
    )
    // 199 < 200 (min beta) → passes
    checker.verify("org.oppia.android", "production", newVersionCode = 199L)
  }

  @Test
  fun testVerify_ga_betweenBetaVersionCodes_throws() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L, 210L))
      )
    )
    // 205 lies between beta's 200 and 210 — must fail (205 > min beta = 200).
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "production", newVersionCode = 205L)
    }
    assertThat(exception).hasMessageThat().contains("200")
  }

  @Test
  fun testVerify_ga_multipleVersionCodesOnAlpha_checksLowest() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(305L, 310L))
      )
    )
    // 304 < 305 (min alpha) → passes
    checker.verify("org.oppia.android", "production", newVersionCode = 304L)
  }

  @Test
  fun testVerify_ga_betweenAlphaVersionCodes_throws() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(305L, 310L))
      )
    )
    // 307 lies between alpha's 305 and 310 — must fail (307 > min alpha = 305).
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "production", newVersionCode = 307L)
    }
    assertThat(exception).hasMessageThat().contains("305")
  }

  // ---------------------------------------------------------------------------
  // Unknown track — should hard-fail rather than silently no-op
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_unknownTrack_throwsWithTrackName() {
    val exception = assertThrows<IllegalStateException>() {
      checker.verify("org.oppia.android", "internal", newVersionCode = 100L)
    }

    assertThat(exception).hasMessageThat().contains("internal")
  }
}
