package org.oppia.android.scripts.release.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the request body for the Google Play Developer API's `edits.tracks.update` endpoint.
 *
 * @property track the Play Console track name (e.g. `"alpha"`, `"beta"`, `"production"`)
 * @property releases the list of release entries to configure on the track
 */
@JsonClass(generateAdapter = true)
data class TrackUpdateRequest(
  @Json(name = "track") val track: String,
  @Json(name = "releases") val releases: List<ReleaseEntry>
) {

  /**
   * Represents a single release entry to assign to the track.
   *
   * @property versionCodes the version codes to include in this release
   * @property status the desired release lifecycle status (e.g. `"completed"`, `"inProgress"`)
   * @property releaseNotes the localised release notes for this release, keyed by BCP-47 language
   *     tag (e.g. `"en-US"`). May be empty if no release notes are provided.
   */
  @JsonClass(generateAdapter = true)
  data class ReleaseEntry(
    @Json(name = "versionCodes") val versionCodes: List<Long>,
    @Json(name = "status") val status: String,
    @Json(name = "releaseNotes") val releaseNotes: List<LocalizedText>,
    /** Only set when [status] is `"inProgress"` (staged rollout). Must be in range (0.0, 1.0). */
    @Json(name = "userFraction") val userFraction: Double? = null
  )

  /**
   * Represents a localised text entry for release notes.
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
