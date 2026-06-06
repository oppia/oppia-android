package org.oppia.android.scripts.release.model

import com.squareup.moshi.JsonClass

/**
 * Represents the response from the Google Play Developer API's `tracks.get` or
 * `edits.tracks.update` endpoint.
 *
 * @property releases the list of release entries currently configured on the track, or null if the
 *     track has no releases
 */
@JsonClass(generateAdapter = true)
data class TrackResponse(val releases: List<ReleaseEntry>?) {

  /**
   * Represents a single release entry within a track.
   *
   * @property versionCodes the version codes included in this release, or null if none are assigned
   * @property status the release lifecycle status (e.g. "completed", "inProgress", "draft",
   *     "halted")
   */
  @JsonClass(generateAdapter = true)
  data class ReleaseEntry(
    val versionCodes: List<Long>?,
    val status: String?
  )
}
