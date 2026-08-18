package org.oppia.android.scripts.release

/**
 * Client for querying GitHub's REST API to determine the CI status of commits on a branch.
 *
 * This interface exists so the candidate-identification logic in [FindAlphaCandidate] can be
 * tested hermetically without making real network calls. The real implementation is
 * [GoogleGitHubCiClient]; tests supply a [FakeGitHubCiClient].
 *
 * API references:
 * - List commits: https://docs.github.com/en/rest/commits/commits#list-commits
 * - Check runs:   https://docs.github.com/en/rest/checks/runs#list-check-runs-for-a-git-reference
 */
interface GitHubCiClient {

  /**
   * Returns up to [limit] commits on [branch] in reverse-chronological order (newest first).
   *
   * @param branch the branch name to query (e.g. "develop")
   * @param limit the maximum number of commits to return; must be in [1, 100]
   * @return the list of [CommitSummary] entries, newest commit first
   */
  fun listCommits(branch: String, limit: Int = 100): List<CommitSummary>

  /**
   * Returns the overall CI status of the commit identified by [commitSha].
   *
   * The status is derived from the GitHub check-runs API. A commit is considered [CiStatus.PASSING]
   * only when every check run has completed with a conclusive success or a skipped outcome.
   * If any run has failed, timed out, or been cancelled the status is [CiStatus.FAILING]. Any
   * run that is still queued or in progress produces [CiStatus.PENDING].
   *
   * @param commitSha the full 40-character SHA of the commit to query
   * @return the [CiStatus] reflecting the collective outcome of all check runs on this commit
   */
  fun getCheckRunStatus(commitSha: String): CiStatus

  /**
   * A lightweight summary of a single commit as returned by the list-commits endpoint.
   *
   * @property sha the full 40-character commit SHA
   */
  data class CommitSummary(val sha: String)

  /**
   * The overall CI result for a specific commit, derived from its GitHub check runs.
   *
   * - [PASSING]   — all check runs completed successfully (or were skipped/neutral)
   * - [FAILING]   — at least one check run completed with a non-success conclusion
   * - [PENDING]   — at least one check run is still queued or in progress (and none have failed)
   * - [NO_CHECKS] — the commit has no check runs at all; this is unexpected in oppia-android
   *   (every push triggers CI) and is treated as a hard error by [FindAlphaCandidate]
   */
  enum class CiStatus {
    /** All check runs for this commit completed with a passing or neutral conclusion. */
    PASSING,

    /** At least one check run completed with a failure, timeout, or cancellation conclusion. */
    FAILING,

    /** At least one check run is still queued or in progress (and none have conclusively failed). */
    PENDING,

    /**
     * This commit has no check runs associated with it. In oppia-android every push triggers CI,
     * so this status is unexpected and indicates either a transient API error or a commit that
     * bypassed the required checks. Callers should treat this as an error, not a silent skip.
     */
    NO_CHECKS,
  }
}
