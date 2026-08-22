package org.oppia.android.scripts.release

/**
 * Precondition checker that verifies the new binary's version code maintains the required
 * cross-track ordering constraint for Oppia Android releases.
 *
 * Per the project specification, after any release the version codes across tracks must satisfy:
 *   **ga < beta < alpha**
 *
 * This checker queries all three tracks and validates that the new version code fits correctly
 * into the ordering, accounting for both live and pending releases on other tracks (since multiple
 * tracks can be deployed simultaneously).
 *
 * Frozen OS-specific version codes (see [FROZEN_VERSION_CODES_PER_TRACK]) are excluded from the
 * ordering constraint. These are permanently active builds for deprecated API levels — they were
 * assigned low version codes before the current version code scheme and do not represent current
 * releases that must participate in the cross-track ordering.
 *
 * Note: Play Console validates version codes within a track but does not enforce the cross-track
 * ordering constraint. This check provides a clearer pre-flight error message and avoids wasting
 * an edit session on a guaranteed API rejection.
 *
 * @param client the [PlayConsoleClient] used to query track releases
 * @param frozenVersionCodesPerTrack version codes to exclude from the inversion check per track;
 *     defaults to [FROZEN_VERSION_CODES_PER_TRACK]
 */
class VersionInversionChecker(
  private val client: PlayConsoleClient,
  private val frozenVersionCodesPerTrack: Map<String, Set<Long>> = FROZEN_VERSION_CODES_PER_TRACK
) {

  /**
   * Verifies that [newVersionCode] satisfies the cross-track ordering constraint when deploying
   * to [targetTrack] for [packageName].
   *
   * Specifically:
   * - Deploying to **alpha**: new vc must be greater than the highest version code on beta and ga
   * - Deploying to **beta**: new vc must be greater than the highest on ga, and less than the
   *   lowest non-frozen alpha version code
   * - Deploying to **production**: new vc must be less than the lowest non-frozen beta and alpha
   *   version codes
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @param targetTrack the Play Console track being deployed to ("alpha", "beta", or "production")
   * @param newVersionCode the version code of the binary about to be uploaded
   * @param existingEditId an optional existing edit ID for the Play Console
   * @throws IllegalStateException if [newVersionCode] violates the cross-track ordering constraint
   */
  fun verify(
    packageName: String,
    targetTrack: String,
    newVersionCode: Long,
    existingEditId: String
  ) {
    val frozenAlpha = frozenVersionCodesPerTrack[ALPHA_TRACK] ?: emptySet()
    val frozenBeta = frozenVersionCodesPerTrack[BETA_TRACK] ?: emptySet()

    // Exclude frozen version codes before computing min/max so that permanently-active
    // OS-specific builds (e.g. the KitKat VC 16 on alpha) do not participate in the
    // cross-track ordering constraint.
    val alphaVersionCodes = client.getTrackReleases(packageName, ALPHA_TRACK, existingEditId)
      .flatMap { it.versionCodes }
      .filterNot { it in frozenAlpha }
    val betaVersionCodes = client.getTrackReleases(packageName, BETA_TRACK, existingEditId)
      .flatMap { it.versionCodes }
      .filterNot { it in frozenBeta }
    val gaVersionCodes = client.getTrackReleases(packageName, GA_TRACK, existingEditId)
      .flatMap { it.versionCodes }
      .filter { it !in frozenGaCodes }

    val maxBeta = betaVersionCodes.maxOrNull()
    val maxGa = gaVersionCodes.maxOrNull()
    val minAlpha = alphaVersionCodes.minOrNull()
    val minBeta = betaVersionCodes.minOrNull()

    when (targetTrack) {
      ALPHA_TRACK -> {
        // Alpha must be strictly greater than beta and ga.
        if (maxBeta != null) {
          check(newVersionCode > maxBeta) {
            "Version inversion: deploying $newVersionCode to alpha but beta has version code " +
              "$maxBeta. Alpha must be strictly greater than beta."
          }
        }
        if (maxGa != null) {
          check(newVersionCode > maxGa) {
            "Version inversion: deploying $newVersionCode to alpha but ga has version code " +
              "$maxGa. Alpha must be strictly greater than ga."
          }
        }
      }
      BETA_TRACK -> {
        // Beta must be strictly greater than ga and strictly less than alpha.
        if (maxGa != null) {
          check(newVersionCode > maxGa) {
            "Version inversion: deploying $newVersionCode to beta but ga has version code " +
              "$maxGa. Beta must be strictly greater than ga."
          }
        }
        if (minAlpha != null) {
          check(newVersionCode < minAlpha) {
            "Version inversion: deploying $newVersionCode to beta but alpha has a release at " +
              "version code $minAlpha. Beta must be strictly less than all alpha version codes."
          }
        }
      }
      GA_TRACK -> {
        // GA must be strictly less than beta and alpha.
        if (minBeta != null) {
          check(newVersionCode < minBeta) {
            "Version inversion: deploying $newVersionCode to ga but beta has a release at " +
              "version code $minBeta. GA must be strictly less than all beta version codes."
          }
        }
        if (minAlpha != null) {
          check(newVersionCode < minAlpha) {
            "Version inversion: deploying $newVersionCode to ga but alpha has a release at " +
              "version code $minAlpha. GA must be strictly less than all alpha version codes."
          }
        }
      }
      else -> error(
        "Unknown track '$targetTrack'. Expected one of: '$ALPHA_TRACK', '$BETA_TRACK', " +
          "'$GA_TRACK'."
      )
    }
  }

  private companion object {
    private const val ALPHA_TRACK = "alpha"
    private const val BETA_TRACK = "beta"
    private const val GA_TRACK = "production"
  }
}
