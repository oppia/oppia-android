package org.oppia.android.scripts.release

/**
 * Precondition checker that prevents uploading a binary when a release is already pending on the
 * target Play Console track.
 *
 * **Allowed cases:**
 * - No releases on the track → always passes.
 * - Only terminal releases (`"completed"` or `"halted"`) → passes; a new binary can be uploaded.
 *
 * **Blocked cases:**
 * - An `"inProgress"` (staged) release exists → fails. The binary deployment workflow is only for
 *   initial deployments. To change the rollout fraction of a live release, use the
 *   `update_rollout.yml` workflow (which calls `UpdateRolloutFraction`) instead.
 * - A `"draft"` or any other non-terminal release exists → fails unconditionally; a pending draft
 *   means the track is not in a clean state for a new upload.
 */
class PendingReleaseChecker(private val client: PlayConsoleClient) {

  /**
   * Verifies that it is safe to upload a new binary on [track].
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @param track the Play Console track to check (e.g. "alpha", "beta", "production")
   * @throws IllegalStateException if the track has a pending or in-progress release
   */
  fun verify(packageName: String, track: String) {
    val releases = client.getTrackReleases(packageName, track)

    if (releases.isEmpty()) return

    val pendingRelease = releases.find { it.status !in TERMINAL_STATUSES }
    check(pendingRelease == null) {
      if (pendingRelease!!.status == "inProgress") {
        "An in-progress release already exists on track '$track' " +
          "(versionCodes=${pendingRelease.versionCodes}, " +
          "rollout=${(pendingRelease.rolloutFraction ?: 0) / 10.0}%). " +
          "To change the rollout fraction of a live release, use the update_rollout.yml " +
          "workflow instead of re-deploying."
      } else {
        "Pending release detected on track '$track': status='${pendingRelease.status}', " +
          "versionCodes=${pendingRelease.versionCodes}. " +
          "Cannot upload a new binary while a release is in-flight."
      }
    }
  }

  private companion object {
    /** Release statuses that indicate a release is no longer actively in-flight. */
    private val TERMINAL_STATUSES = setOf("completed", "halted")
  }
}
