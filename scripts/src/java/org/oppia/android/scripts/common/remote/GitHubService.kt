package org.oppia.android.scripts.common.remote

import org.oppia.android.scripts.common.model.GitHubIssue
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.Headers

interface GitHubService {

  // Reference: https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#list-repository-issues.
  // Sort by ascending creation date by default to attempt improving stability (e.g. slight robustness against new issues being filed) since the GitHub API doesn't seem to provide support for stable pagination.
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
