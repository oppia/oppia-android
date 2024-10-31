package org.oppia.android.scripts.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Moshi data structure representing a remote issue on GitHub.
 *
 * @property number the unique number corresponding to this issue (i.e. the number listed after
 *     'issues/' in an issue's GitHub URL)
 */
@JsonClass(generateAdapter = true)
data class GitHubIssue(
  @Json(name = "number") val number: Int,
  @Json(name = "pull_request") val pullRequest: PullRequest? = null
)

// Define PullRequest class as needed, or leave it empty if you don’t require specific details
@JsonClass(generateAdapter = true)
data class PullRequest(
  // Add fields if needed, or leave empty for filtering purposes
)
