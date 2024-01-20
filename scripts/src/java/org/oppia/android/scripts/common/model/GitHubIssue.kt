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
data class GitHubIssue(@Json(name = "number") val number: Int)
