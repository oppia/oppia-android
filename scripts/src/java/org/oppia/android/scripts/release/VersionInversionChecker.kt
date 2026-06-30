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
 * Note: Play Console validates version codes within a track but does not enforce the cross-track
 * ordering constraint. This check provides a clearer pre-flight error message and avoids wasting
 * an edit session on a guaranteed API rejection.
 */
class VersionInversionChecker(private val client: PlayConsoleClient) {

  /**
   * Verifies that [newVersionCode] satisfies the cross-track ordering constraint when deploying
   * to [targetTrack] for [packageName].
   *
   * Specifically:
   * - Deploying to **alpha**: new vc must be greater than the highest version code on beta and ga
   * - Deploying to **beta**: new vc must be greater than the highest on ga, and less than the
   *   lowest pending/live alpha version code
   * - Deploying to **production**: new vc must be less than the lowest beta and alpha version codes
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @param targetTrack the Play Console track being deployed to ("alpha", "beta", or "production")
   * @param newVersionCode the version code of the binary about to be uploaded
   * @throws IllegalStateException if [newVersionCode] violates the cross-track ordering constraint
   */
  fun verify(packageName: String, targetTrack: String, newVersionCode: Long) {
    val alphaVersionCodes = client.getTrackReleases(packageName, ALPHA_TRACK)
      .flatMap { it.versionCodes }
    val betaVersionCodes = client.getTrackReleases(packageName, BETA_TRACK)
      .flatMap { it.versionCodes }
    val gaVersionCodes = client.getTrackReleases(packageName, GA_TRACK)
      .flatMap { it.versionCodes }

    val maxAlpha = alphaVersionCodes.maxOrNull()
    val maxBeta = betaVersionCodes.maxOrNull()
    val maxGa = gaVersionCodes.maxOrNull()

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
        if (maxAlpha != null) {
          check(newVersionCode < maxAlpha) {
            "Version inversion: deploying $newVersionCode to beta but alpha has version code " +
              "$maxAlpha. Beta must be strictly less than alpha."
          }
        }
      }
      GA_TRACK -> {
        // GA must be strictly less than beta and alpha.
        if (maxBeta != null) {
          check(newVersionCode < maxBeta) {
            "Version inversion: deploying $newVersionCode to ga but beta has version code " +
              "$maxBeta. GA must be strictly less than beta."
          }
        }
        if (maxAlpha != null) {
          check(newVersionCode < maxAlpha) {
            "Version inversion: deploying $newVersionCode to ga but alpha has version code " +
              "$maxAlpha. GA must be strictly less than alpha."
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
