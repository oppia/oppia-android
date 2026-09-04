package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.After
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

  @After
  fun tearDown() {
    fakeClient.close()
  }

  // ---------------------------------------------------------------------------
  // Deploying to ALPHA — must be greater than beta and ga
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_alpha_allTracksEmpty_passes() {
    // No existing releases on any track — any version code is valid.
    checker.verify(
      "org.oppia.android",
      "alpha",
      newVersionCode = 1L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_alpha_greaterThanBeta_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    checker.verify(
      "org.oppia.android",
      "alpha",
      newVersionCode = 201L,
      existingEditId = "test-edit"
    )
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
    checker.verify(
      "org.oppia.android",
      "alpha",
      newVersionCode = 201L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_alpha_lowerThanBeta_throwsMentionsBeta() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(300L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify(
        "org.oppia.android",
        "alpha",
        newVersionCode = 299L,
        existingEditId = "test-edit"
      )
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
      checker.verify(
        "org.oppia.android",
        "alpha",
        newVersionCode = 499L,
        existingEditId = "test-edit"
      )
    }
    assertThat(exception).hasMessageThat().contains("ga")
    assertThat(exception).hasMessageThat().contains("500")
  }

  // ---------------------------------------------------------------------------
  // Deploying to BETA — must be greater than ga and less than alpha
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_beta_allTracksEmpty_passes() {
    checker.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 100L,
      existingEditId = "test-edit"
    )
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
    checker.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 200L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_beta_lowerThanGa_throws() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify(
        "org.oppia.android",
        "beta",
        newVersionCode = 100L,
        existingEditId = "test-edit"
      )
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
      checker.verify(
        "org.oppia.android",
        "beta",
        newVersionCode = 300L,
        existingEditId = "test-edit"
      )
    }
    assertThat(exception).hasMessageThat().contains("alpha")
    assertThat(exception).hasMessageThat().contains("200")
    assertThat(exception).hasMessageThat().contains("all alpha version codes")
  }

  // ---------------------------------------------------------------------------
  // Deploying to GA (production) — must be less than beta and alpha
  // ---------------------------------------------------------------------------

  // ---------------------------------------------------------------------------
  // Frozen version codes — ordering constraints must ignore confirmed code
  // (See FrozenReleaseConfig: alpha={16L, 201L}, beta={196L})
  // ---------------------------------------------------------------------------

  @Test
  fun testVerify_ga_allTracksEmpty_passes() {
    checker.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 100L,
      existingEditId = "test-edit"
    )
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
    checker.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 100L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_ga_greaterThanBeta_throws() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(200L)))
    )
    val exception = assertThrows<IllegalStateException>() {
      checker.verify(
        "org.oppia.android",
        "production",
        newVersionCode = 300L,
        existingEditId = "test-edit"
      )
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
      checker.verify(
        "org.oppia.android",
        "production",
        newVersionCode = 400L,
        existingEditId = "test-edit"
      )
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
    checker.verify(
      "org.oppia.android",
      "alpha",
      newVersionCode = 201L,
      existingEditId = "test-edit"
    )
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
      checker.verify(
        "org.oppia.android",
        "alpha",
        newVersionCode = 300L,
        existingEditId = "test-edit"
      )
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
    checker.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 304L,
      existingEditId = "test-edit"
    )
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
      checker.verify(
        "org.oppia.android",
        "beta",
        newVersionCode = 307L,
        existingEditId = "test-edit"
      )
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
    checker.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 199L,
      existingEditId = "test-edit"
    )
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
      checker.verify(
        "org.oppia.android",
        "production",
        newVersionCode = 205L,
        existingEditId = "test-edit"
      )
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
    checker.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 304L,
      existingEditId = "test-edit"
    )
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
      checker.verify(
        "org.oppia.android",
        "production",
        newVersionCode = 307L,
        existingEditId = "test-edit"
      )
    }
    assertThat(exception).hasMessageThat().contains("305")
  }

  @Test
  fun testVerify_beta_alphaHasFrozenAndCurrentCodes_useNonFrozenMinAlpha() {
    // Alpha has frozen codes {16L, 201L} plus a current non-frozen build at 205L.
    // Without frozen exclusion, minAlpha would be 16 and the check (204 < 16) would fail.
    // With frozen exclusion, minAlpha = 205 and (204 < 205) passes.
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "completed",
          versionCodes = listOf(16L, 201L, 205L)
        )
      )
    )
    val checkerWithFrozen = VersionInversionChecker(
      fakeClient, mapOf("alpha" to setOf(16L, 201L))
    )
    checkerWithFrozen.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 204L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_beta_alphaHasOnlyFrozenCodes_noUpperBoundConstraint() {
    // Alpha only has frozen codes {16L, 201L}; after filtering, alphaVersionCodes is empty
    // so minAlpha = null and there is no upper-bound constraint on the new beta.
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "completed",
          versionCodes = listOf(16L, 201L)
        )
      )
    )
    val checkerWithFrozen = VersionInversionChecker(
      fakeClient, mapOf("alpha" to setOf(16L, 201L))
    )
    // 500 would normally violate alpha constraint if frozen codes were not excluded.
    checkerWithFrozen.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 500L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_ga_betaHasFrozenAndCurrentCodes_useNonFrozenMinBeta() {
    // Beta has frozen code {196L} plus a current non-frozen build at 200L.
    // Without frozen exclusion, minBeta = 196 and (199 < 196) would fail.
    // With frozen exclusion, minBeta = 200 and (199 < 200) passes.
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "completed",
          versionCodes = listOf(196L, 200L)
        )
      )
    )
    val checkerWithFrozen = VersionInversionChecker(
      fakeClient, mapOf("beta" to setOf(196L))
    )
    checkerWithFrozen.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 199L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_ga_alphaHasFrozenAndCurrentCodes_useNonFrozenMinAlpha() {
    // Alpha has frozen codes {16L, 201L} plus a current non-frozen build at 205L.
    // Without frozen exclusion, minAlpha = 16 and (204 < 16) would fail.
    // With frozen exclusion, minAlpha = 205 and (204 < 205) passes.
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "completed",
          versionCodes = listOf(16L, 201L, 205L)
        )
      )
    )
    val checkerWithFrozen = VersionInversionChecker(
      fakeClient, mapOf("alpha" to setOf(16L, 201L))
    )
    checkerWithFrozen.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 204L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_alpha_betaHasOnlyFrozenCodes_noLowerBoundConstraint() {
    // Beta only has the frozen code {196L}; after filtering, betaVersionCodes is empty so
    // maxBeta = null and there is no lower-bound constraint on the new alpha.
    // Without frozen exclusion, maxBeta = 196 and check(100 > 196) would fail.
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(
          status = "completed",
          versionCodes = listOf(196L)
        )
      )
    )
    val checkerWithFrozen = VersionInversionChecker(
      fakeClient, mapOf("beta" to setOf(196L))
    )
    // 100 < 196 but 196 is a frozen code and must not constrain alpha deployment.
    checkerWithFrozen.verify(
      "org.oppia.android",
      "alpha",
      newVersionCode = 100L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_unknownTrack_throwsWithTrackName() {
    val exception = assertThrows<IllegalStateException>() {
      checker.verify(
        "org.oppia.android",
        "internal",
        newVersionCode = 100L,
        existingEditId = "test-edit"
      )
    }

    assertThat(exception).hasMessageThat().contains("internal")
  }

  @Test
  fun testVerify_existingEditId_isPassedToAllThreeTrackQueries() {
    checker.verify(
      "org.oppia.android", "alpha", newVersionCode = 100L, existingEditId = "upload-edit-42"
    )

    assertThat(fakeClient.queriedEditIds).hasSize(3)
    assertThat(fakeClient.queriedEditIds).containsExactly(
      "upload-edit-42", "upload-edit-42", "upload-edit-42"
    )
  }

  @Test
  fun testVerify_beta_frozenAlphaVcLowerThanNewBeta_passes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(16L)), // frozen
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(40000L)) // live
      )
    )
    val frozenCodes = mapOf("alpha" to setOf(16L))
    val checkerWithFrozenCodes = VersionInversionChecker(fakeClient, frozenCodes)

    checkerWithFrozenCodes.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 37301L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_beta_frozenAlphaVcOnlyAlphaRelease_treatsAlphaAsEmpty_passes() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(16L)))
    )
    val frozenCodes = mapOf("alpha" to setOf(16L))
    val checkerWithFrozenCodes = VersionInversionChecker(fakeClient, frozenCodes)

    checkerWithFrozenCodes.verify(
      "org.oppia.android",
      "beta",
      newVersionCode = 37301L,
      existingEditId = "test-edit"
    )
  }

  @Test
  fun testVerify_beta_nonFrozenAlphaVcLowerThanNewBeta_stillFails() {
    // The exclusion must NOT suppress a genuine inversion on a non-frozen alpha release.
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(16L)), // frozen
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(100L)) // live
      )
    )
    val frozenCodes = mapOf("alpha" to setOf(16L))
    val checkerWithFrozenCodes = VersionInversionChecker(fakeClient, frozenCodes)

    val exception = assertThrows<IllegalStateException>() {
      checkerWithFrozenCodes.verify(
        "org.oppia.android",
        "beta",
        newVersionCode = 200L,
        existingEditId = "test-edit"
      )
    }
    assertThat(exception).hasMessageThat().isEqualTo(
      "Version inversion: deploying 200 to beta but alpha has a release at version code 100." +
        " Beta must be strictly less than all alpha version codes."
    )
  }

  @Test
  fun testVerify_ga_frozenBetaVcLowerThanNewGa_passes() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(16L)), // frozen
        PlayConsoleClient.TrackRelease(status = "completed", versionCodes = listOf(40000L)) // live
      )
    )
    val frozenCodes = mapOf("beta" to setOf(16L))
    val checkerWithFrozenCodes = VersionInversionChecker(fakeClient, frozenCodes)

    checkerWithFrozenCodes.verify(
      "org.oppia.android",
      "production",
      newVersionCode = 37000L,
      existingEditId = "test-edit"
    )
  }
}
