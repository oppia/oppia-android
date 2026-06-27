package org.oppia.android.scripts.release

/**
 * Client for interacting with the Google Play Developer Publishing API.
 *
 * All write operations on the Play Console are performed within an edit session: callers must
 * first call [createEdit], perform any number of [uploadAab] and [setTrackRelease] operations, and
 * then call [commitEdit] to publish the changes. Changes are discarded if the session expires or
 * [commitEdit] is never called.
 *
 * API reference: https://developers.google.com/android-publisher/api-ref/rest
 */
interface PlayConsoleClient {
  /**
   * Creates a new edit session for [packageName] and returns its edit ID.
   *
   * @param packageName the application package name (e.g. "org.oppia.android")
   * @return the edit ID of the newly created session
   */
  fun createEdit(packageName: String): String

  /**
   * Returns all active releases on [track] for [packageName], sorted by version code descending.
   *
   * @param packageName the application package name
   * @param track the Play Console track to query (e.g. "alpha", "beta", "production")
   * @return the list of [TrackRelease] entries currently live on the track
   */
  fun getTrackReleases(packageName: String, track: String): List<TrackRelease>

  /**
   * Uploads the AAB at [aabPath] within [editId] and returns its assigned version code.
   *
   * @param packageName the application package name
   * @param editId the active edit session ID returned by [createEdit]
   * @param aabPath absolute local path to the signed AAB file to upload
   * @return the version code assigned to the uploaded binary by the Play Console
   */
  fun uploadAab(packageName: String, editId: String, aabPath: String): Long

  /**
   * Assigns the uploaded binary to [track] within [editId] with the specified release metadata.
   *
   * Must be called after [uploadAab] and before [commitEdit].
   *
   * @param packageName the application package name
   * @param editId the active edit session ID returned by [createEdit]
   * @param track the Play Console track (e.g. "alpha", "beta", "production")
   * @param versionCode the version code of the binary to assign, as returned by [uploadAab]
   * @param rolloutFraction the rollout fraction as an integer in the range [0, 1000], where
   *     1000 means full rollout (status: "completed") and any value below 1000 produces a staged
   *     rollout (status: "inProgress"). For example: 250 = 25%, 334 = 33.4%, 1000 = 100%.
   * @param releaseNotes map of BCP-47 language codes to release notes text (max 500 chars each)
   */
  fun setTrackRelease(
    packageName: String,
    editId: String,
    track: String,
    versionCode: Long,
    rolloutFraction: Int,
    releaseNotes: Map<String, String>
  )

  /**
   * Commits [editId], publishing all changes made within the session to the Play Console.
   *
   * This is the final step of any edit session. Once committed, changes are irreversible.
   *
   * @param packageName the application package name
   * @param editId the active edit session ID returned by [createEdit]
   */
  fun commitEdit(packageName: String, editId: String)

  /**
   * Represents a single release entry on a Play Console track.
   *
   * @property versionCodes the version codes included in this release
   * @property status the release lifecycle status (e.g. "completed", "inProgress", "draft",
   *     "halted")
   */
  data class TrackRelease(
    val versionCodes: List<Long>,
    val status: String
  )
}
