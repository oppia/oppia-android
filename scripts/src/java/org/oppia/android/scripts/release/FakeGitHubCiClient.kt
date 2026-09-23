package org.oppia.android.scripts.release

/**
 * Test-only fake implementation of [GitHubCiClient].
 *
 * Lets unit tests for [findAlphaCandidate] control commit history and per-commit CI statuses
 * without making real network calls to GitHub's API.
 *
 * Usage in tests:
 * ```
 * val client = FakeGitHubCiClient()
 * client.setCommits("sha-a", "sha-b", "sha-c")
 * client.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
 * client.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)
 * assertThat(findAlphaCandidate(client, "develop")).isEqualTo("sha-b")
 * ```
 */
class FakeGitHubCiClient : GitHubCiClient {

  /** Commit history returned by [listCommits], newest first. */
  private val commits = mutableListOf<GitHubCiClient.CommitSummary>()

  /** Per-SHA CI status map; defaults to [GitHubCiClient.CiStatus.NO_CHECKS] for unknown SHAs. */
  private val statusMap = mutableMapOf<String, GitHubCiClient.CiStatus>()

  /**
   * Sets the commit history that [listCommits] will return.
   *
   * @param shas commit SHAs in newest-first order
   */
  fun setCommits(vararg shas: String) {
    commits.clear()
    shas.mapTo(commits) { GitHubCiClient.CommitSummary(sha = it) }
  }

  /**
   * Configures the CI status [getCheckRunStatus] returns for the given [sha].
   *
   * @param sha commit SHA to configure
   * @param status the [GitHubCiClient.CiStatus] to return for that SHA
   */
  fun setStatus(sha: String, status: GitHubCiClient.CiStatus) {
    statusMap[sha] = status
  }

  override fun listCommits(
    branch: String,
    perPage: Int,
    page: Int
  ): List<GitHubCiClient.CommitSummary> {
    val startIndex = (page - 1) * perPage
    return commits.drop(startIndex).take(perPage)
  }

  override fun getCheckRunStatus(commitSha: String): GitHubCiClient.CiStatus =
    statusMap[commitSha] ?: GitHubCiClient.CiStatus.NO_CHECKS
}
