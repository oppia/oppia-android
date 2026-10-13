package org.oppia.android.scripts.release.remote

import org.oppia.android.scripts.release.model.CheckRunsResponse
import org.oppia.android.scripts.release.model.CommitListEntry
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the subset of GitHub's REST API used by [FindAlphaCandidate].
 *
 * Only the two endpoints needed for CI candidate identification are declared here:
 * - List commits on a branch (to walk the commit graph)
 * - List check runs for a specific commit (to evaluate CI status)
 *
 * API version header `X-GitHub-Api-Version: 2022-11-28` is sent on every request per GitHub's
 * versioning policy. The `Accept` header requests the standard JSON response format.
 *
 * API reference: https://docs.github.com/en/rest
 */
interface GitHubCiService {

  /**
   * Lists commits on [branch] in reverse-chronological order (newest first).
   *
   * Results are paginated; set [perPage] to at most 100 and increment [page] to fetch additional
   * pages.
   *
   * API reference:
   * https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#list-commits
   *
   * @param owner the repository owner (e.g. "oppia")
   * @param repo the repository name (e.g. "oppia-android")
   * @param branch the branch name or SHA to list commits from (e.g. "develop")
   * @param authorizationBearer the `Bearer <token>` header value for authentication
   * @param perPage number of results per page (max 100)
   * @param page the 1-indexed page number to retrieve
   * @return a [Call] wrapping a list of [CommitListEntry] objects
   */
  @Headers("Accept: application/vnd.github+json", "X-GitHub-Api-Version: 2022-11-28")
  @GET("repos/{owner}/{repo}/commits")
  fun listCommits(
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Query("sha") branch: String,
    @Header("Authorization") authorizationBearer: String,
    @Query("per_page") perPage: Int,
    @Query("page") page: Int
  ): Call<List<CommitListEntry>>

  /**
   * Lists all check runs associated with [ref] (a branch name or full commit SHA).
   *
   * Results are paginated. The caller must collect all pages to ensure that every check run
   * is evaluated — a single failing run on a later page would otherwise be missed.
   *
   * API reference:
   * https://docs.github.com/en/rest/checks/runs?apiVersion=2022-11-28#list-check-runs-for-a-git-reference
   *
   * @param owner the repository owner
   * @param repo the repository name
   * @param ref the full commit SHA or branch name to query
   * @param authorizationBearer the `Bearer <token>` header value for authentication
   * @param perPage number of results per page (max 100)
   * @param page the 1-indexed page number to retrieve
   * @return a [Call] wrapping a [CheckRunsResponse] containing the total count and this page's runs
   */
  @Headers("Accept: application/vnd.github+json", "X-GitHub-Api-Version: 2022-11-28")
  @GET("repos/{owner}/{repo}/commits/{ref}/check-runs")
  fun listCheckRuns(
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Path("ref") ref: String,
    @Header("Authorization") authorizationBearer: String,
    @Query("per_page") perPage: Int,
    @Query("page") page: Int
  ): Call<CheckRunsResponse>
}
