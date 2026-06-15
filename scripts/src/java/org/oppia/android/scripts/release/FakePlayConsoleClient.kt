package org.oppia.android.scripts.release

/**
 * In-memory fake implementation of [PlayConsoleClient] for use in unit tests.
 *
 * Records all API calls for verification and returns pre-configured responses. Callers can inspect
 * the recorded state via [createdEdits], [uploadedBundles], [trackUpdates], and [committedEdits].
 * Error conditions can be simulated by setting [shouldFailNextCall] to true.
 *
 * Track releases returned by [getTrackReleases] can be configured per-track via
 * [setTrackReleases].
 */
class FakePlayConsoleClient : PlayConsoleClient {

  /** Whether the next API call should throw an [IllegalStateException] to simulate a failure. */
  var shouldFailNextCall = false

  /** The edit ID to return from [createEdit]. Incremented after each call. */
  var nextEditId = 1

  /** All edit session IDs created via [createEdit], in order. */
  val createdEdits = mutableListOf<String>()

  /** All bundles uploaded via [uploadAab], as (packageName, editId, aabPath) triples. */
  val uploadedBundles = mutableListOf<Triple<String, String, String>>()

  /** All track updates via [setTrackRelease], as recorded [TrackUpdate] entries. */
  val trackUpdates = mutableListOf<TrackUpdate>()

  /** All edit IDs committed via [commitEdit], in order. */
  val committedEdits = mutableListOf<String>()

  private var nextVersionCode = 1L
  private val trackReleasesMap = mutableMapOf<String, List<PlayConsoleClient.TrackRelease>>()

  override fun createEdit(packageName: String): String {
    maybeFailCall("createEdit")
    val editId = "fake-edit-${nextEditId++}"
    createdEdits.add(editId)
    return editId
  }

  override fun getTrackReleases(
    packageName: String,
    track: String
  ): List<PlayConsoleClient.TrackRelease> {
    maybeFailCall("getTrackReleases")
    return trackReleasesMap[track] ?: emptyList()
  }

  override fun uploadAab(packageName: String, editId: String, aabPath: String): Long {
    maybeFailCall("uploadAab")
    uploadedBundles.add(Triple(packageName, editId, aabPath))
    return nextVersionCode++
  }

  override fun setTrackRelease(
    packageName: String,
    editId: String,
    track: String,
    versionCode: Long,
    rolloutFraction: Double,
    releaseNotes: Map<String, String>
  ) {
    maybeFailCall("setTrackRelease")
    trackUpdates.add(TrackUpdate(packageName, editId, track, versionCode, rolloutFraction, releaseNotes))
  }

  override fun commitEdit(packageName: String, editId: String) {
    maybeFailCall("commitEdit")
    committedEdits.add(editId)
  }

  /**
   * Configures the releases returned by [getTrackReleases] for the given [track].
   *
   * @param track the Play Console track name (e.g. "alpha", "beta", "production")
   * @param releases the list of [PlayConsoleClient.TrackRelease] entries to return
   */
  fun setTrackReleases(track: String, releases: List<PlayConsoleClient.TrackRelease>) {
    trackReleasesMap[track] = releases
  }

  /**
   * Configures the next version code returned by [uploadAab].
   *
   * @param versionCode the version code to return on the next upload
   */
  fun setNextVersionCode(versionCode: Long) {
    nextVersionCode = versionCode
  }

  /** Resets all recorded state and configuration to defaults. */
  fun reset() {
    shouldFailNextCall = false
    nextEditId = 1
    nextVersionCode = 1L
    createdEdits.clear()
    uploadedBundles.clear()
    trackUpdates.clear()
    committedEdits.clear()
    trackReleasesMap.clear()
  }

  private fun maybeFailCall(methodName: String) {
    if (shouldFailNextCall) {
      shouldFailNextCall = false
      error("FakePlayConsoleClient: simulated failure in $methodName")
    }
  }

  /**
   * Records a single [setTrackRelease] invocation for test verification.
   *
   * @property packageName the application package name
   * @property editId the edit session ID
   * @property track the Play Console track
   * @property versionCode the version code assigned to the track
   * @property rolloutFraction the staged rollout fraction (1.0 = full rollout)
   * @property releaseNotes the release notes map (language code → text)
   */
  data class TrackUpdate(
    val packageName: String,
    val editId: String,
    val track: String,
    val versionCode: Long,
    val rolloutFraction: Double,
    val releaseNotes: Map<String, String>
  )
}
