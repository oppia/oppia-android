package org.oppia.android.scripts.release

/**
 * Precondition checker that prevents uploading a binary when a release is already pending on the
 * target Play Console track, unless the upload is a staged rollout increase for the same version.
 *
 * **Allowed cases:**
 * - No releases on the track → always passes.
 * - Only terminal releases (`"completed"` or `"halted"`) → passes; a new binary can be uploaded.
 * - An `"inProgress"` (staged) release exists AND [newRolloutFraction] is strictly greater than
 *   the current rollout fraction → passes; this is a legitimate rollout-percentage increase.
 *
 * **Blocked cases:**
 * - A `"draft"` or any other non-terminal release exists → fails unconditionally; a pending draft
 *   means the track is not in a clean state for a new upload.
 * - An `"inProgress"` release exists but [newRolloutFraction] ≤ current rollout → fails; the
 *   caller must use a strictly higher fraction to increase the rollout.
 */
class PendingReleaseChecker(private val client: PlayConsoleClient) {

  /**
   * Verifies that it is safe to upload a new binary (or increase rollout) on [track].
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @param track the Play Console track to check (e.g. "alpha", "beta", "production")
   * @param newRolloutFraction the rollout fraction of the upload being attempted, as an integer
   *     in [0, 1000] (e.g. 250 = 25%, 1000 = 100%). Used to validate rollout increases when an
   *     in-progress release already exists on the track.
   * @throws IllegalStateException if the track has a pending release that cannot be safely
   *     overridden by the requested upload
   */
  fun verify(packageName: String, track: String, newRolloutFraction: Int) {
    val releases = client.getTrackReleases(packageName, track)

    if (releases.isEmpty()) return

    val inProgressRelease = releases.find { it.status == "inProgress" }
    if (inProgressRelease != null) {
      val currentRollout = inProgressRelease.rolloutFraction ?: 0
      check(newRolloutFraction > currentRollout) {
        "An in-progress release already exists on track '$track' at " +
          "${currentRollout / 10.0}% rollout (versionCodes=${inProgressRelease.versionCodes}). " +
          "To increase the rollout, use a rollout_fraction greater than $currentRollout " +
          "(got $newRolloutFraction)."
      }
      println(
        "In-progress release detected on track '$track'. Proceeding with rollout increase " +
          "from ${currentRollout / 10.0}% to ${newRolloutFraction / 10.0}%."
      )
      return
    }

    val pendingRelease = releases.find { it.status !in TERMINAL_STATUSES }
    check(pendingRelease == null) {
      "Pending release detected on track '$track': status='${pendingRelease!!.status}', " +
        "versionCodes=${pendingRelease.versionCodes}. " +
        "Cannot upload a new binary while a release is in-flight."
    }
  }

  private companion object {
    /** Release statuses that indicate a release is no longer actively in-flight. */
    private val TERMINAL_STATUSES = setOf("completed", "halted")
  }
}
