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
  @Json(name = "pull_request") val pullRequest: PullRequestInfo? = null
)

/**
 * Data class representing information about a pull request associated with a GitHub issue.
 *
 * @property url the URL of the pull request, if it exists. This provides the link to the specific
 *     pull request associated with the issue on GitHub.
 */
@JsonClass(generateAdapter = true)
data class PullRequestInfo(
  @Json(name = "url") val url: String? = null
)
