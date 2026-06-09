package org.oppia.android.scripts.release

/**
 * Precondition check that prevents uploading a binary when a release is already pending on the
 * target Play Console track.
 *
 * A pending release is any release whose status is not "completed" or "halted" (i.e. a release
 * that is in "draft", "inProgress", or any other transient state). Uploading a new binary while
 * a pending release exists would conflict with the in-flight release and may cause unexpected
 * rollout behaviour or Play Console API errors.
 */
class PendingReleaseCheck(private val client: PlayConsoleClient) {

  /**
   * Verifies that there is no pending release on [track] for [packageName].
   *
   * A release is considered pending if its status is anything other than "completed" or "halted".
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @param track the Play Console track to check (e.g. "alpha", "beta", "production")
   * @throws IllegalStateException if a pending release is detected on the track
   */
  fun verify(packageName: String, track: String) {
    val releases = client.getTrackReleases(packageName, track)

    if (releases.isEmpty()) {
      println("No existing releases on track '$track'. Pending release check passed.")
      return
    }

    val pendingRelease = releases.find { it.status !in TERMINAL_STATUSES }

    check(pendingRelease == null) {
      "Pending release detected on track '$track': status='${pendingRelease!!.status}', " +
        "versionCodes=${pendingRelease.versionCodes}. " +
        "Cannot upload a new binary while a release is in-flight."
    }

    println("Pending release check passed: no in-flight releases on track '$track'.")
  }

  private companion object {
    /** Release statuses that indicate a release is no longer actively in-flight. */
    private val TERMINAL_STATUSES = setOf("completed", "halted")
  }
}
