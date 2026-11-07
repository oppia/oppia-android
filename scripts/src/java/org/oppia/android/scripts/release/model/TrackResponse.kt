package org.oppia.android.scripts.release.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the response from the Google Play Developer API's `tracks.get` or
 * `edits.tracks.update` endpoint.
 *
 * @property releases the list of release entries currently configured on the track, or null if the
 *     track has no releases
 */
@JsonClass(generateAdapter = true)
data class TrackResponse(
  @Json(name = "releases") val releases: List<ReleaseEntry>? // null when the track has no releases
) {

  /**
   * Represents a single release entry within a track.
   *
   * @property versionCodes the version codes included in this release, or null if no version codes
   *     have been assigned (e.g. an empty draft release)
   * @property status the release lifecycle status. One of: `"statusUnspecified"`, `"draft"`,
   *     `"inProgress"`, `"halted"`, or `"completed"`.
   * @property userFraction the fraction of users receiving this release in a staged rollout
   *     (range 0.0-1.0, e.g. 0.25 for 25%), or null for completed/halted releases
   * @property releaseNotes the localised release notes for this release, or null if none were set
   */
  @JsonClass(generateAdapter = true)
  data class ReleaseEntry(
    @Json(name = "versionCodes") val versionCodes: List<String>?,
    @Json(name = "status") val status: String,
    @Json(name = "userFraction") val userFraction: Double? = null,
    @Json(name = "releaseNotes") val releaseNotes: List<LocalizedText>? = null
  )

  /**
   * Represents a localised text entry, used for release notes.
   *
   * @property language the BCP-47 language tag (e.g. `"en-US"`)
   * @property text the release note text for that language
   */
  @JsonClass(generateAdapter = true)
  data class LocalizedText(
    @Json(name = "language") val language: String,
    @Json(name = "text") val text: String
  )
}
