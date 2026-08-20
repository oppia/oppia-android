package org.oppia.android.scripts.release

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.oppia.android.scripts.release.model.CheckRunsResponse
import org.oppia.android.scripts.release.remote.GitHubCiService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Real [GitHubCiClient] implementation backed by GitHub's REST API.
 *
 * [overrideApiBaseUrl] redirects all traffic to a local test server instead of
 * [GITHUB_API_BASE_URL]; set only in tests.
 *
 * @property accessToken GitHub PAT or Actions `GITHUB_TOKEN` used for Bearer authentication
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

  /** Fetches all pages of check runs for [commitSha] and returns them as a flat list. */
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
   * Derives an overall [GitHubCiClient.CiStatus] from a list of check runs, ignoring runs
   * from scheduled (cron) workflows. Priority: NO_CHECKS → PENDING → FAILING → PASSING.
   */
  private fun deriveStatus(runs: List<CheckRunsResponse.CheckRun>): GitHubCiClient.CiStatus {
    // Exclude runs triggered by cron workflows — they run independently of commit pushes and
    // are not required status checks. Including them can cause a legitimately passing commit
    // to appear as FAILING if a cron suite happened to fail on that same commit SHA.
    // Runs with a null event (field absent from API response) are kept to be safe.
    val relevantRuns = runs.filter { it.checkSuite?.event != "schedule" }

    if (relevantRuns.isEmpty()) return GitHubCiClient.CiStatus.NO_CHECKS

    // A run is "pending" if it hasn't reached a terminal state yet.
    val hasPending = relevantRuns.any { it.status != "completed" }
    if (hasPending) return GitHubCiClient.CiStatus.PENDING

    // All runs are "completed" at this point — evaluate their conclusions.
    val hasFailure = relevantRuns.any { it.conclusion !in PASSING_CONCLUSIONS }
    if (hasFailure) return GitHubCiClient.CiStatus.FAILING

    return GitHubCiClient.CiStatus.PASSING
  }

  companion object {
    /** The base URL for GitHub's public REST API. */
    const val GITHUB_API_BASE_URL = "https://api.github.com/"

    /** Check-run conclusions treated as non-blocking: success, skipped, neutral. */
    private val PASSING_CONCLUSIONS = setOf("success", "skipped", "neutral")
  }
}
