package org.oppia.android.scripts.release

/**
 * Represents the outcome of an alpha candidate search.
 *
 * The three cases must be handled distinctly by callers:
 * - [Found] – a candidate was found; its SHA is safe to use for a new alpha cut.
 * - [NoNewCommits] – [findAlphaCandidate] was called with a `sinceSha` reference and no commits
 *   exist on the branch after that point. This is not a failure; the current latest-alpha is
 *   already up to date and no new cut is needed.
 * - [NoPassingCommit] – commits were inspected but none had fully-passing CI. This indicates a
 *   CI health issue on the branch that requires coordinator attention.
 */
sealed class AlphaCandidateResult {
  /** A passing candidate was found at [sha]. */
  data class Found(val sha: String) : AlphaCandidateResult()

  /**
   * No commits exist on the branch after the `sinceSha` reference (e.g. latest-alpha tag).
   * The existing alpha is already the most recent passing commit; no cut is needed.
   */
  object NoNewCommits : AlphaCandidateResult()

  /**
   * Commits were inspected but none had fully-passing CI. [commitsChecked] is the number of
   * commits that were evaluated.
   */
  data class NoPassingCommit(val commitsChecked: Int) : AlphaCandidateResult()
}

/**
 * Script that walks the most recent commits on a branch and prints the SHA of the newest commit
 * whose GitHub CI check runs are all passing — the "alpha deployment candidate".
 *
 * Exit codes:
 *   0 – candidate found (SHA printed to stdout) OR no new commits since latest-alpha (nothing to do)
 *   1 – no passing candidate found; reason printed to stderr
 *
 * Usage:
 *   bazel run //scripts:find_alpha_candidate -- <github_token> [branch] [commit_limit]
 *       [latest_alpha_sha] [override_api_base_url]
 *
 * Positional arguments:
 *   github_token          – GitHub PAT or Actions GITHUB_TOKEN for API authentication
 *   branch                – branch to search (default: "develop")
 *   commit_limit          – number of recent commits to inspect (default: 50, max: 100)
 *   latest_alpha_sha      – full SHA of the current latest-alpha tag; only commits strictly
 *                           newer than this SHA are considered. Pass "" (empty) to search all
 *                           recent commits without a reference point.
 *   override_api_base_url – optional base URL for the GitHub API (default: https://api.github.com).
 *                           Intended for testing only; do not set in production.
 */
fun main(args: Array<String>) {
  require(args.isNotEmpty()) {
    "Usage: find_alpha_candidate <github_token> [branch] [commit_limit] " +
      "[latest_alpha_sha] [override_api_base_url]"
  }
  val githubToken = args[0]
  val branch = if (args.size > 1) args[1] else "develop"
  val commitLimit = if (args.size > 2) {
    args[2].toIntOrNull()
      ?: error("commit_limit must be an integer, got '${args[2]}'")
  } else 50
  val latestAlphaSha = if (args.size > 3) args[3].takeIf { it.isNotEmpty() } else null
  val overrideApiBaseUrl = if (args.size > 4) args[4] else null

  val client: GitHubCiClient = GoogleGitHubCiClient(
    accessToken = githubToken,
    overrideApiBaseUrl = overrideApiBaseUrl
  )
  when (
    val result = findAlphaCandidate(
      gitHubCiClient = client,
      branch = branch,
      commitLimit = commitLimit,
      sinceSha = latestAlphaSha
    )
  ) {
    is AlphaCandidateResult.Found -> println(result.sha)
    is AlphaCandidateResult.NoNewCommits -> {
      System.err.println(
        "latest-alpha is already the most recent passing commit on '$branch'. No new cut needed."
      )
      // Exit 0 — not a failure; the calling workflow should skip dispatch.
    }
    is AlphaCandidateResult.NoPassingCommit -> {
      System.err.println(
        "No passing candidate found in the last ${result.commitsChecked} commit(s) on '$branch'. " +
          "Ensure CI has completed for recent commits and retry."
      )
      System.exit(1)
    }
  }
}

/**
 * Walks commits on [branch] (newest first) and returns an [AlphaCandidateResult] describing
 * the outcome of the candidate search.
 *
 * When [sinceSha] is provided, only commits that appear strictly before [sinceSha] in the
 * newest-first commit list (i.e., commits newer than [sinceSha]) are inspected. If no such
 * commits exist, [AlphaCandidateResult.NoNewCommits] is returned immediately without making
 * any check-run API calls — indicating that the current latest-alpha is already up to date.
 *
 * This function is separated from [main] so that unit tests can inject a [FakeGitHubCiClient]
 * and verify the candidate-selection logic without making real network calls.
 *
 * Commits with statuses other than [GitHubCiClient.CiStatus.PASSING] are skipped:
 * - [GitHubCiClient.CiStatus.FAILING]   – the commit should not be deployed
 * - [GitHubCiClient.CiStatus.PENDING]   – CI hasn't finished; a completed commit may follow
 * - [GitHubCiClient.CiStatus.NO_CHECKS] – no check runs found; every oppia-android commit should
 *   have CI, so this is treated as an unexpected error rather than silently skipped
 *
 * @param gitHubCiClient client used to query commit lists and check-run statuses
 * @param branch the branch to walk (e.g. "develop")
 * @param commitLimit number of recent commits to fetch; must be in [1, 100]
 * @param sinceSha full SHA of the reference commit (e.g. current latest-alpha tag). Only commits
 *     strictly newer than this SHA are inspected. `null` means all fetched commits are inspected.
 * @return an [AlphaCandidateResult] describing the outcome
 */
fun findAlphaCandidate(
  gitHubCiClient: GitHubCiClient,
  branch: String,
  commitLimit: Int = 50,
  sinceSha: String? = null
): AlphaCandidateResult {
  require(commitLimit in 1..100) {
    "commitLimit must be in [1, 100], got $commitLimit"
  }

  System.err.println("=== Find Alpha Candidate ===")
  System.err.println("  Branch       : $branch")
  System.err.println("  Commit limit : $commitLimit")
  sinceSha?.let { System.err.println("  Since SHA    : ${it.take(7)}") }

  val allCommits = gitHubCiClient.listCommits(branch, limit = commitLimit)

  // If sinceSha is provided, only inspect commits that appear BEFORE sinceSha in the
  // newest-first list (commits strictly newer than the reference point).
  val commits = if (sinceSha != null) {
    val cutIndex = allCommits.indexOfFirst { it.sha == sinceSha }
    if (cutIndex == -1) allCommits else allCommits.take(cutIndex)
  } else allCommits

  if (sinceSha != null && commits.isEmpty()) {
    System.err.println("  No new commits since ${sinceSha.take(7)}. latest-alpha is up to date.")
    return AlphaCandidateResult.NoNewCommits
  }

  System.err.println("  Inspecting ${commits.size} commit(s) (newest first)…")

  for (commit in commits) {
    val status = gitHubCiClient.getCheckRunStatus(commit.sha)
    System.err.println("  ${commit.sha.take(7)} → $status")
    when (status) {
      GitHubCiClient.CiStatus.PASSING -> {
        System.err.println("  ✓ Candidate : ${commit.sha}")
        return AlphaCandidateResult.Found(commit.sha)
      }
      GitHubCiClient.CiStatus.NO_CHECKS -> error(
        "Commit ${commit.sha.take(7)} on '$branch' has no CI check runs. " +
          "Every commit in oppia-android should trigger CI; this likely indicates a " +
          "transient API error or a commit that bypassed the required checks. " +
          "Re-run the workflow once CI has populated check runs for this commit."
      )
      else -> { /* FAILING or PENDING — skip and inspect the next commit */ }
    }
  }

  System.err.println("  ✗ No passing candidate found.")
  return AlphaCandidateResult.NoPassingCommit(commits.size)
}
