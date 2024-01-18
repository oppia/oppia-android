package org.oppia.android.scripts.common.remote

import org.oppia.android.scripts.common.model.GitHubIssue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit service for interacting with GitHub's REST API. */
interface GitHubService {

  /**
   * Fetches [countPerPage] open issues from the configured GitHub repository.
   *
   * Note that the returned issues are sorted in ascending order by creation date as a basic attempt
   * to improve API stability (e.g. robustness against new issues being filed when fetching multiple
   * pages of issues) since the GitHub API doesn't seem to provide support for stable pagination.
   *
   * API reference:
   * https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#list-repository-issues.
   *
   * @param repoOwner the owner of the repository to be read (e.g. 'oppia')
   * @param repoName the name of the repository to be read (e.g. 'oppia-android')
   * @param authorizationBearer the authorization access token bearer to allow repository reading
   * @param pageNumber the current page number to read (starting at 1)
   * @param countPerPage the number of issues to read per page (defaults to 100)
   * @return the list of [GitHubIssue]s read from the remote repository (as a [Call])
   */
  @Headers("Accept: application/vnd.github+json", "X-GitHub-Api-Version: 2022-11-28")
  @GET("repos/{repo_owner}/{repo_name}/issues?direction=asc")
  fun fetchOpenIssues(
    @Path("repo_owner") repoOwner: String,
    @Path("repo_name") repoName: String,
    @Header("Authorization") authorizationBearer: String,
    @Query("page") pageNumber: Int,
    @Query("per_page") countPerPage: Int = 100
  ): Call<List<GitHubIssue>>
}
