package org.oppia.android.scripts.release

/**
 * Script that walks the most recent commits on a branch and prints the SHA of the newest commit
 * whose GitHub CI check runs are all passing — the "alpha deployment candidate".
 *
 * The script exits with code 0 and prints the candidate SHA to stdout on success.
 * It exits with code 1 (and a message to stderr) if no passing commit is found within the
 * requested commit window.
 *
 * Usage:
 *   bazel run //scripts:find_alpha_candidate -- <github_token> [branch] [commit_limit] [override_api_base_url]
 *
 * Positional arguments:
 *   github_token          – GitHub PAT or Actions GITHUB_TOKEN for API authentication
 *   branch                – branch to search (default: "develop")
 *   commit_limit          – number of recent commits to inspect (default: 50, max: 100)
 *   override_api_base_url – optional base URL for the GitHub API (default: https://api.github.com).
 *                           Intended for testing only; do not set in production.
 *
 * Exit codes:
 *   0 – candidate found; its full 40-character SHA is printed to stdout
 *   1 – no passing candidate found within the commit window; reason printed to stderr
 */
fun main(args: Array<String>) {
  require(args.isNotEmpty()) {
    "Usage: find_alpha_candidate <github_token> [branch] [commit_limit] [override_api_base_url]"
  }
  val githubToken = args[0]
  val branch = if (args.size > 1) args[1] else "develop"
  val commitLimit = if (args.size > 2) {
    args[2].toIntOrNull()
      ?: error("commit_limit must be an integer, got '${args[2]}'")
  } else 50
  val overrideApiBaseUrl = if (args.size > 3) args[3] else null

  val client: GitHubCiClient = GoogleGitHubCiClient(
    accessToken = githubToken,
    overrideApiBaseUrl = overrideApiBaseUrl
  )
  val candidateSha = findAlphaCandidate(
    gitHubCiClient = client,
    branch = branch,
    commitLimit = commitLimit
  )
  if (candidateSha == null) {
    System.err.println(
      "No passing candidate found in the last $commitLimit commits on '$branch'. " +
        "Ensure CI has completed for recent commits and retry."
    )
    System.exit(1)
  } else {
    println(candidateSha)
  }
}

/**
 * Walks the most recent [commitLimit] commits on [branch] from newest to oldest and returns the
 * SHA of the first commit whose CI check runs are all [GitHubCiClient.CiStatus.PASSING].
 *
 * This function is separated from [main] so that unit tests can inject a [FakeGitHubCiClient]
 * and verify the candidate-selection logic without making real network calls.
 *
 * Commits with statuses other than [GitHubCiClient.CiStatus.PASSING] are skipped:
 * - [GitHubCiClient.CiStatus.FAILING]   – the commit should not be deployed
 * - [GitHubCiClient.CiStatus.PENDING]   – CI hasn't finished; a completed commit may follow
 * - [GitHubCiClient.CiStatus.NO_CHECKS] – no CI data; the commit is not ready for deployment
 *
 * @param gitHubCiClient client used to query commit lists and check-run statuses
 * @param branch the branch to walk (e.g. "develop")
 * @param commitLimit number of commits to inspect; must be in [1, 100]
 * @return the full 40-character SHA of the first fully-passing commit, or `null` if none found
 */
fun findAlphaCandidate(
  gitHubCiClient: GitHubCiClient,
  branch: String,
  commitLimit: Int = 50
): String? {
  require(commitLimit in 1..100) {
    "commitLimit must be in [1, 100], got $commitLimit"
  }

  println("=== Find Alpha Candidate ===")
  println("  Branch       : $branch")
  println("  Commit limit : $commitLimit")

  val commits = gitHubCiClient.listCommits(branch, limit = commitLimit)
  println("  Inspecting ${commits.size} commits (newest first)…")

  for (commit in commits) {
    val status = gitHubCiClient.getCheckRunStatus(commit.sha)
    println("  ${commit.sha.take(7)} → $status")
    if (status == GitHubCiClient.CiStatus.PASSING) {
      println("  ✓ Candidate : ${commit.sha}")
      return commit.sha
    }
  }

  println("  ✗ No passing candidate found.")
  return null
}
