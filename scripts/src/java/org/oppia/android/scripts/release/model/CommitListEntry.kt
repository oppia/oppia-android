package org.oppia.android.scripts.release.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the response from GitHub's
 * `GET /repos/{owner}/{repo}/commits` endpoint.
 *
 * Each entry in the list contains the full commit SHA and the short message needed to identify
 * and log the commit.
 *
 * API reference:
 * https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#list-commits
 *
 * @property sha the full 40-character commit SHA
 * @property commit the nested commit object containing the message
 */
@JsonClass(generateAdapter = true)
data class CommitListEntry(
  @Json(name = "sha") val sha: String,
  @Json(name = "commit") val commit: CommitDetails
) {

  /**
   * Nested commit details object embedded within each list-commits response entry.
   *
   * @property message the full commit message (first line is the subject)
   */
  @JsonClass(generateAdapter = true)
  data class CommitDetails(
    @Json(name = "message") val message: String
  )
}
