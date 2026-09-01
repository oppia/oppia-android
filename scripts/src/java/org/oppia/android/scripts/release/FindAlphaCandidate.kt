package org.oppia.android.scripts.release

/**
 * Represents the outcome of an alpha candidate search.
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
 *   0 – candidate found (SHA printed to stdout), no new commits since latest-alpha, or no passing
 *       commit found among the inspected commits (all three are "nothing to do" outcomes)
 *   1 – fatal error (e.g. API failure); reason printed to stderr
 *
 * Usage:
 *   bazel run //scripts:find_alpha_candidate -- <github_token> [branch]
 *       [latest_alpha_sha] [override_api_base_url]
 *
 * Positional arguments:
 *   github_token          – GitHub PAT or Actions GITHUB_TOKEN for API authentication
 *   branch                – branch to search (default: "develop")
 *   latest_alpha_sha      – full SHA of the current latest-alpha tag; only commits strictly
 *                           newer than this SHA are considered. Pass "" (empty) to search all
 *                           commits without a reference point.
 *   override_api_base_url – optional base URL for the GitHub API (default: https://api.github.com).
 *                           Intended for testing only; do not set in production.
 */
fun main(args: Array<String>) {
  require(args.isNotEmpty()) {
    "Usage: find_alpha_candidate <github_token> [branch] [latest_alpha_sha] [override_api_base_url]"
  }
  val githubToken = args[0]
  val branch = if (args.size > 1) args[1] else "develop"
  val latestAlphaSha = if (args.size > 2) args[2].takeIf { it.isNotEmpty() } else null
  val overrideApiBaseUrl = if (args.size > 3) args[3] else null

  val client: GitHubCiClient = GoogleGitHubCiClient(
    accessToken = githubToken,
    overrideApiBaseUrl = overrideApiBaseUrl
  )
  try {
    when (
      val result = findAlphaCandidate(
        gitHubCiClient = client,
        branch = branch,
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
          "No passing candidate found in the last ${result.commitsChecked} commit(s) on " +
            "'$branch'. CI may still be running — no new release will be cut this cycle."
        )
        // Exit 0 — not a failure. The calling workflow checks for an empty candidate SHA
        // and skips the tag-push and dispatch steps automatically.
      }
    }
  } catch (e: IllegalStateException) {
    System.err.println("Fatal: ${e.message}")
    System.exit(1)
  }
}

/**
 * Walks commits on [branch] (newest first) using paginated API requests and returns an
 * [AlphaCandidateResult].
 *
 * When [sinceSha] is provided, only commits strictly newer than that SHA are inspected.
 * If no such commits exist, [AlphaCandidateResult.NoNewCommits] is returned immediately.
 *
 * Commits are fetched 100 at a time. Pagination continues until a passing commit is found,
 * [sinceSha] is encountered, or the commit history is exhausted.
 *
 * Separated from [main] so unit tests can inject a [FakeGitHubCiClient] without real
 * network calls.
 *
 * @param gitHubCiClient client used to query commit lists and check-run statuses
 * @param branch the branch to walk (e.g. "develop")
 * @param sinceSha reference SHA (e.g. current latest-alpha tag); only commits strictly newer
 *     than this are inspected. `null` inspects all commits.
 * @return an [AlphaCandidateResult] describing the outcome
 */
fun findAlphaCandidate(
  gitHubCiClient: GitHubCiClient,
  branch: String,
  sinceSha: String? = null
): AlphaCandidateResult {
  System.err.println("=== Find Alpha Candidate ===")
  System.err.println("  Branch    : $branch")
  sinceSha?.let { System.err.println("  Since SHA : ${it.take(7)}") }

  var page = 1
  var commitsChecked = 0

  while (true) {
    val pageCommits = gitHubCiClient.listCommits(branch, perPage = 100, page = page)

    // Locate sinceSha on this page so we stop before inspecting commits at or older than it.
    val cutIndex = if (sinceSha != null) pageCommits.indexOfFirst { it.sha == sinceSha } else -1
    val toInspect = if (cutIndex == -1) pageCommits else pageCommits.take(cutIndex)

    // sinceSha is the very first commit on page 1 → develop HEAD == sinceSha, no new commits.
    if (page == 1 && cutIndex == 0) {
      System.err.println(
        "  No new commits since ${sinceSha!!.take(7)}. latest-alpha is up to date."
      )
      return AlphaCandidateResult.NoNewCommits
    }

    System.err.println("  Page $page: inspecting ${toInspect.size} commit(s)…")

    for (commit in toInspect) {
      commitsChecked++
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

    if (cutIndex != -1) break // sinceSha found on this page; no further pages needed.
    if (pageCommits.size < 100) break // Fewer than 100 results means this was the last page.

    page++
  }

  // If we completed the loop having checked 0 commits while sinceSha was given, sinceSha must
  // have been found at the start of a later page (i.e. 0 commits are newer than it).
  if (commitsChecked == 0 && sinceSha != null) {
    System.err.println(
      "  No new commits since ${sinceSha.take(7)}. latest-alpha is up to date."
    )
    return AlphaCandidateResult.NoNewCommits
  }

  System.err.println("  ✗ No passing candidate found in $commitsChecked commit(s).")
  return AlphaCandidateResult.NoPassingCommit(commitsChecked)
}
