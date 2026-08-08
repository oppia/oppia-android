package org.oppia.android.scripts.release

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.oppia.android.scripts.release.model.CheckRunsResponse
import org.oppia.android.scripts.release.remote.GitHubCiService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Real [GitHubCiClient] implementation that talks to GitHub's REST API over HTTPS.
 *
 * Authentication is performed via a GitHub personal access token (PAT) or GitHub Actions
 * `GITHUB_TOKEN` passed as [accessToken]. The token is sent as a `Bearer` credential on every
 * request and never logged.
 *
 * Two API endpoints are used:
 * - `GET /repos/{owner}/{repo}/commits` — walks the branch commit graph
 * - `GET /repos/{owner}/{repo}/commits/{ref}/check-runs` — evaluates CI status per commit
 *
 * Both endpoints are paginated. [listCommits] fetches a single page of up to [limit] results.
 * [getCheckRunStatus] fetches all pages of check runs so that no run is missed even when the
 * total exceeds 100.
 *
 * The [overrideApiBaseUrl] constructor parameter is provided so tests can route all traffic
 * through a local [MockWebServer] instead of the real GitHub endpoint, following the same
 * pattern as [GooglePlayConsoleClient].
 *
 * @property accessToken a GitHub PAT or Actions `GITHUB_TOKEN` used for Bearer authentication
 * @property repoOwner the GitHub repository owner (defaults to "oppia")
 * @property repoName the GitHub repository name (defaults to "oppia-android")
 * @property overrideApiBaseUrl optional URL override for tests; defaults to [GITHUB_API_BASE_URL]
 */
class GoogleGitHubCiClient(
  private val accessToken: String,
  private val repoOwner: String = "oppia",
  private val repoName: String = "oppia-android",
  private val overrideApiBaseUrl: String? = null
) : GitHubCiClient {

  private val authorizationBearer: String get() = "Bearer $accessToken"

  private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .callTimeout(90, TimeUnit.SECONDS)
      .build()
  }

  private val moshi: Moshi by lazy { Moshi.Builder().build() }

  private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(overrideApiBaseUrl ?: GITHUB_API_BASE_URL)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .client(okHttpClient)
      .build()
  }

  private val service: GitHubCiService by lazy { retrofit.create(GitHubCiService::class.java) }

  override fun listCommits(branch: String, limit: Int): List<GitHubCiClient.CommitSummary> {
    require(limit in 1..100) { "limit must be in [1, 100], got $limit." }
    val response = service.listCommits(
      owner = repoOwner,
      repo = repoName,
      branch = branch,
      authorizationBearer = authorizationBearer,
      perPage = limit,
      page = 1
    ).execute()
    check(response.isSuccessful) {
      "Failed to list commits for branch '$branch': HTTP ${response.code()}\n" +
        "${response.errorBody()?.string()}"
    }
    return checkNotNull(response.body()) {
      "GitHub returned an empty body for branch '$branch' commit list."
    }.map { entry -> GitHubCiClient.CommitSummary(sha = entry.sha) }
  }

  override fun getCheckRunStatus(commitSha: String): GitHubCiClient.CiStatus {
    val allRuns = fetchAllCheckRunPages(commitSha)
    return deriveStatus(allRuns)
  }

  /**
   * Fetches every page of check-run results for [commitSha], collecting them into a single list.
   *
   * The GitHub check-runs API paginates at 100 results per page. A full run of CI for
   * oppia-android can easily exceed 100 jobs, so all pages must be collected before the
   * overall status can be evaluated.
   */
  private fun fetchAllCheckRunPages(commitSha: String): List<CheckRunsResponse.CheckRun> {
    val allRuns = mutableListOf<CheckRunsResponse.CheckRun>()
    var page = 1
    while (true) {
      val response = service.listCheckRuns(
        owner = repoOwner,
        repo = repoName,
        ref = commitSha,
        authorizationBearer = authorizationBearer,
        perPage = 100,
        page = page
      ).execute()
      check(response.isSuccessful) {
        "Failed to fetch check runs for commit '$commitSha' (page $page): " +
          "HTTP ${response.code()}\n${response.errorBody()?.string()}"
      }
      val body = checkNotNull(response.body()) {
        "GitHub returned an empty body for check runs of commit '$commitSha' (page $page)."
      }
      allRuns.addAll(body.checkRuns)
      // Stop paginating once we have collected all runs or the page returned fewer than 100 items
      // (indicating it was the last page).
      if (allRuns.size >= body.totalCount || body.checkRuns.size < 100) break
      page++
    }
    return allRuns
  }

  /**
   * Derives the overall [GitHubCiClient.CiStatus] from the full list of [CheckRunsResponse.CheckRun]s.
   *
   * Evaluation rules (applied in priority order):
   * 1. No runs → [GitHubCiClient.CiStatus.NO_CHECKS]
   * 2. Any run with [status] not `"completed"` → [GitHubCiClient.CiStatus.PENDING]
   *    (checked before failures so a mix of pending + failure is not prematurely concluded)
   * 3. Any completed run with a conclusive non-success [conclusion] → [GitHubCiClient.CiStatus.FAILING]
   * 4. All runs completed with success/skipped/neutral → [GitHubCiClient.CiStatus.PASSING]
   */
  private fun deriveStatus(runs: List<CheckRunsResponse.CheckRun>): GitHubCiClient.CiStatus {
    if (runs.isEmpty()) return GitHubCiClient.CiStatus.NO_CHECKS

    // A run is "pending" if it hasn't reached a terminal state yet.
    val hasPending = runs.any { it.status != "completed" }
    if (hasPending) return GitHubCiClient.CiStatus.PENDING

    // All runs are "completed" at this point — evaluate their conclusions.
    val hasFailure = runs.any { it.conclusion !in PASSING_CONCLUSIONS }
    if (hasFailure) return GitHubCiClient.CiStatus.FAILING

    return GitHubCiClient.CiStatus.PASSING
  }

  companion object {
    /** The base URL for GitHub's public REST API. */
    const val GITHUB_API_BASE_URL = "https://api.github.com/"

    /**
     * Conclusion values that are treated as non-blocking for the purposes of candidate selection.
     *
     * - `"success"` — the check explicitly passed
     * - `"skipped"` — the check was deliberately skipped (e.g. a path filter excluded it)
     * - `"neutral"` — the check completed without a definitive pass/fail signal
     */
    private val PASSING_CONCLUSIONS = setOf("success", "skipped", "neutral")
  }
}
