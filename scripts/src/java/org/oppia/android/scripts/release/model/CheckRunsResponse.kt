package org.oppia.android.scripts.release.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the response from GitHub's
 * `GET /repos/{owner}/{repo}/commits/{ref}/check-runs` endpoint.
 *
 * The response is paginated; the real client fetches all pages until [totalCount] items have been
 * collected. A commit is considered CI-passing only when all runs reach a conclusive state.
 *
 * API reference:
 * https://docs.github.com/en/rest/checks/runs?apiVersion=2022-11-28#list-check-runs-for-a-git-reference
 *
 * @property totalCount the total number of check runs associated with this ref (across all pages)
 * @property checkRuns the check-run entries returned in this page
 */
@JsonClass(generateAdapter = true)
data class CheckRunsResponse(
  @Json(name = "total_count") val totalCount: Int,
  @Json(name = "check_runs") val checkRuns: List<CheckRun>
) {

  /**
   * A single GitHub Actions check run entry.
   *
   * @property id the unique numeric identifier of this check run
   * @property name the human-readable name of the check (e.g. "Robolectric tests (shard 1)")
   * @property status the lifecycle status of the run:
   *     `"queued"`, `"in_progress"`, or `"completed"`
   * @property conclusion the final outcome of the run, or null when [status] is not `"completed"`.
   *     Expected values: `"success"`, `"failure"`, `"neutral"`, `"cancelled"`,
   *     `"skipped"`, `"timed_out"`, `"action_required"`, `"stale"`
   */
  @JsonClass(generateAdapter = true)
  data class CheckRun(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "conclusion") val conclusion: String?
  )
}
