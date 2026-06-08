package org.oppia.android.scripts.release

/**
 * Precondition check that prevents uploading a binary whose version code is not strictly greater
 * than all version codes currently live on the target Play Console track.
 *
 * Version inversion (uploading a lower version code than what's already on the track) would cause
 * the Play Console API to reject the release or, worse, silently roll back users to an older
 * version. This check catches the problem before the upload attempt.
 */
class VersionInversionCheck(private val client: PlayConsoleClient) {

  /**
   * Verifies that [newVersionCode] is strictly greater than all version codes currently live on
   * [track] for [packageName].
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @param track the Play Console track to check (e.g. "alpha", "beta", "production")
   * @param newVersionCode the version code of the binary about to be uploaded
   * @throws IllegalStateException if [newVersionCode] is less than or equal to any live version
   *     code on the track, or if the track state cannot be determined
   */
  fun verify(packageName: String, track: String, newVersionCode: Long) {
    val releases = client.getTrackReleases(packageName, track)
    val liveVersionCodes = releases.flatMap { it.versionCodes }

    if (liveVersionCodes.isEmpty()) {
      println("No existing releases on track '$track'. Version inversion check passed.")
      return
    }

    val highestLiveVersion = checkNotNull(liveVersionCodes.maxOrNull()) {
      "Track '$track' has releases but no version codes. This is unexpected."
    }
    check(newVersionCode > highestLiveVersion) {
      "Version inversion detected: new version code $newVersionCode is not greater than the " +
        "highest live version code $highestLiveVersion on track '$track'. " +
        "All live version codes: $liveVersionCodes"
    }

    println(
      "Version inversion check passed: $newVersionCode > $highestLiveVersion (track '$track')"
    )
  }
}
