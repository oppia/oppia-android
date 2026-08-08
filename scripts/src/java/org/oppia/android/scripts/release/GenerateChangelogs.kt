package org.oppia.android.scripts.release

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File

/**
 * Script that automatically generates a changelog for the previous app version whenever the minor
 * version is bumped in `version.bzl`, and proposes it as a pull request on `develop`.
 *
 * ### How it works
 * 1. Reads the current `MINOR_VERSION` from `version.bzl` (e.g. `18` after a `0.17 → 0.18` bump).
 * 2. Derives the *previous* version (`0.17`) — the version whose changelog must be generated
 *    (since `0.18` is the new unreleased developer build and `0.17` is soon to be released).
 * 3. Collects all PRs merged into `develop` between the two most recent release branch merge-bases.
 * 4. Parses `Fixes #NNNN` / `Closes #NNNN` references from commit messages to include issue titles.
 * 5. Invokes Vertex AI (Gemini) with the above material to produce a 2–3 sentence user-facing
 *    summary of the release changes.
 * 6. If the Vertex AI call fails, falls back to a raw commit-list changelog marked with
 *    `<!-- LLM generation failed -->` so a human can fill in the summary later.
 * 7. Writes `config/changelogs/<major>.<minor>.md` and creates (or updates) a PR targeting
 *    `develop` on the **upstream** repository branch `automated/changelog-<major>.<minor>`.
 *    The PR description links all reference material so reviewers can adjust the LLM context.
 *
 * ### Usage (called by `generate_changelog.yml` via Bazel)
 * ```
 * bazel run //scripts:generate_changelogs -- \
 *   <workspace_root> <gcp_project> <gcp_location> <vertex_model> <gcp_access_token>
 * ```
 *
 * ### Arguments (positional)
 *   0. `workspace_root`  — absolute path to the local repository root
 *   1. `gcp_project`     — GCP project ID that has Vertex AI enabled
 *   2. `gcp_location`    — Vertex AI region (e.g. "us-central1")
 *   3. `vertex_model`    — Vertex AI model ID (e.g. "gemini-1.5-flash")
 *   4. `gcp_access_token`— GCP Bearer token for authenticating with Vertex AI
 *
 * An optional 6th argument overrides the Vertex AI API base URL; this is used in integration
 * tests to route HTTP calls through a local mock server.
 */
fun main(args: Array<String>) {
  require(args.size in 5..6) {
    "Usage: generate_changelogs <workspace_root> <gcp_project> <gcp_location> " +
      "<vertex_model> <gcp_access_token>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspaceRoot = args[0]
  val gcpProject = args[1]
  val gcpLocation = args[2]
  val vertexModel = args[3]
  val gcpAccessToken = args[4]

  val overrideApiBaseUrl = if (args.size == 6) args[5] else null

  // TARGET_VERSION is set by the workflow when the user triggers workflow_dispatch with a
  // specific version (e.g. "0.17"). When empty or absent, version is derived from version.bzl.
  val changelogVersionOverride = System.getenv("TARGET_VERSION")?.takeIf { it.isNotBlank() }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val vertexAiClient = if (overrideApiBaseUrl != null) {
      GoogleVertexAiClient(gcpProject, gcpLocation, vertexModel, gcpAccessToken, overrideApiBaseUrl)
    } else {
      GoogleVertexAiClient(gcpProject, gcpLocation, vertexModel, gcpAccessToken)
    }
    generateChangelogs(
      workspaceRoot = File(workspaceRoot),
      commandExecutor = commandExecutor,
      vertexAiClient = vertexAiClient,
      changelogVersionOverride = changelogVersionOverride
    )
  }
}

/**
 * Orchestrates the full changelog generation workflow.
 *
 * This is separated from [main] so that tests can inject a [FakeVertexAiClient] and a
 * [FakeCommandExecutor] without executing the real `main` entry point.
 *
 * @param workspaceRoot the root directory of the local repository
 * @param commandExecutor the executor for shell commands (`git`, `gh`)
 * @param vertexAiClient the Vertex AI client for generating the changelog summary
 */
fun generateChangelogs(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  vertexAiClient: VertexAiClient,
  changelogVersionOverride: String? = null
) {
  // Step 1 — Determine which version's changelog to generate.
  // If changelogVersionOverride is set (from TARGET_VERSION env / workflow_dispatch input),
  // use it directly. Otherwise derive the version from version.bzl.
  val majorVersion: Int
  val prevMinor: Int
  val changelogVersion: String

  if (changelogVersionOverride != null) {
    val parts = changelogVersionOverride.split(".")
    require(parts.size == 2 && parts.all { it.toIntOrNull() != null }) {
      "TARGET_VERSION must be in 'MAJOR.MINOR' format (e.g. '0.17'), " +
        "got: '$changelogVersionOverride'"
    }
    majorVersion = parts[0].toInt()
    prevMinor = parts[1].toInt()
    changelogVersion = changelogVersionOverride
  } else {
    val (major, minor) = parseVersionBzl(workspaceRoot)
    majorVersion = major
    prevMinor = minor - 1
    check(prevMinor >= 0) {
      "Cannot generate changelog: MINOR_VERSION in version.bzl is $minor. " +
        "Expected a value ≥ 1 (need a previous version to generate a changelog for)."
    }
    changelogVersion = "$majorVersion.$prevMinor"
  }
  val changelogFileName = "$changelogVersion.md"
  val changelogFile = File(workspaceRoot, "$CHANGELOGS_DIR/$changelogFileName")

  println("=== Generate Changelog ===")
  println("  Changelog for   : $changelogVersion")
  println()

  // Step 2 — Bail early if changelog already exists (idempotency guard).
  if (changelogFile.exists()) {
    println(
      "Changelog $changelogFileName already exists at ${changelogFile.path}. Nothing to do."
    )
    return
  }

  // Step 3 — Find the commit range: commits merged since the previous release branch diverged.
  val releaseBranch = "release-$changelogVersion"
  val prevReleaseBranch = "release-$majorVersion.${prevMinor - 1}"

  println("Finding commit range between $prevReleaseBranch and $releaseBranch on develop...")
  val (fromSha, toSha) = findCommitRange(
    workspaceRoot, commandExecutor, releaseBranch, prevReleaseBranch, prevMinor
  )
  println("  From SHA : $fromSha")
  println("  To SHA   : $toSha")
  println()

  // Step 4 — Collect merged PRs and linked issues in that range.
  println("Collecting PRs and issues in commit range...")
  val commits = collectCommitsBetween(workspaceRoot, commandExecutor, fromSha, toSha)
  val prEntries = parsePrEntries(commits)
  val issueNumbers = parseFixedIssueNumbers(commits)
  println("  Found ${prEntries.size} PR(s), ${issueNumbers.size} referenced issue(s).")
  println()

  // Step 5 — Build the LLM prompt and invoke Vertex AI. Fall back on failure.
  val prListText = if (prEntries.isEmpty()) {
    "(none)"
  } else {
    prEntries.joinToString("\n") { "- ${it.title} (#${it.number})" }
  }
  val issueListText = if (issueNumbers.isEmpty()) {
    "(none)"
  } else {
    issueNumbers.joinToString("\n") { "- #$it" }
  }
  val (summary, llmFailed) = invokeLlmWithFallback(
    vertexAiClient, buildPrompt(changelogVersion, prListText, issueListText)
  )

  // Step 6 — Write the changelog file.
  val changelogContent = buildChangelogContent(
    summary = summary,
    prEntries = prEntries,
    issueNumbers = issueNumbers,
    llmFailed = llmFailed
  )
  changelogFile.parentFile.mkdirs()
  changelogFile.writeText(changelogContent)
  println("Wrote changelog to: ${changelogFile.path}")
  println()

  // Step 7 — Push branch and create (or update) the PR.
  val branchName = "automated/changelog-$changelogVersion"
  val prBody = buildPrBody(
    version = changelogVersion,
    fromSha = fromSha,
    toSha = toSha,
    prEntries = prEntries,
    issueNumbers = issueNumbers,
    llmFailed = llmFailed
  )
  createOrUpdateChangelogPr(
    workspaceRoot = workspaceRoot,
    commandExecutor = commandExecutor,
    branchName = branchName,
    changelogFile = changelogFile,
    changelogVersion = changelogVersion,
    prBody = prBody
  )
}

/**
 * Reads `version.bzl` from [workspaceRoot] and extracts the `MAJOR_VERSION` and `MINOR_VERSION`
 * values.
 *
 * @return a pair of (majorVersion, minorVersion) integers
 * @throws IllegalStateException if either value cannot be found or parsed
 */
fun parseVersionBzl(workspaceRoot: File): Pair<Int, Int> {
  val versionBzl = File(workspaceRoot, "version.bzl")
  check(versionBzl.exists()) { "version.bzl not found at: ${versionBzl.absolutePath}" }
  val content = versionBzl.readText()
  val major = MAJOR_VERSION_REGEX.find(content)?.groupValues?.get(1)?.toIntOrNull()
    ?: error("Could not parse MAJOR_VERSION from version.bzl")
  val minor = MINOR_VERSION_REGEX.find(content)?.groupValues?.get(1)?.toIntOrNull()
    ?: error("Could not parse MINOR_VERSION from version.bzl")
  return major to minor
}

/**
 * Computes the `fromSha..toSha` range for the changelog commit collection.
 *
 * The **toSha** is the merge-base of [releaseBranch] and `develop` — the point where the current
 * release branched off (i.e. all commits up to and including the version bump commit).
 *
 * The **fromSha** is the merge-base of [prevReleaseBranch] and `develop` — the point where the
 * *previous* release branched off. If the previous release branch doesn't exist (first release),
 * falls back to the very first commit on `develop`.
 *
 * @param prevMinor the previous minor version number, used to detect the first-release edge case
 * @return a (fromSha, toSha) pair of full commit SHAs
 */
fun findCommitRange(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  releaseBranch: String,
  prevReleaseBranch: String,
  prevMinor: Int
): Pair<String, String> {
  val toSha = gitMergeBase(workspaceRoot, commandExecutor, releaseBranch, "$REMOTE/$DEVELOP_BRANCH")
  val fromSha = if (prevMinor <= 0) {
    // First-ever release: include all commits from the beginning of develop.
    gitFirstCommit(workspaceRoot, commandExecutor)
  } else {
    try {
      gitMergeBase(workspaceRoot, commandExecutor, prevReleaseBranch, "$REMOTE/$DEVELOP_BRANCH")
    } catch (e: IllegalStateException) {
      // Re-throw if this isn't a "branch not found" failure — don't mask unrelated errors.
      if ("unknown revision" !in (e.message ?: "") &&
        "ambiguous argument" !in (e.message ?: "")
      ) throw e
      // Previous release branch doesn't exist on the remote — fall back to first commit.
      println(
        "WARNING: Previous release branch '$prevReleaseBranch' not found on remote. " +
          "Collecting from the beginning of develop."
      )
      gitFirstCommit(workspaceRoot, commandExecutor)
    }
  }
  return fromSha to toSha
}

private fun gitMergeBase(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  ref1: String,
  ref2: String
): String {
  val result = commandExecutor.executeCommand(workspaceRoot, "git", "merge-base", ref1, ref2)
  check(result.exitCode == 0) {
    "git merge-base $ref1 $ref2 failed (exit ${result.exitCode}):\n" +
      result.output.joinToString("\n")
  }
  return result.output.first().trim()
}

private fun gitFirstCommit(workspaceRoot: File, commandExecutor: CommandExecutor): String {
  val result = commandExecutor.executeCommand(
    workspaceRoot, "git", "rev-list", "--max-parents=0", "HEAD"
  )
  check(result.exitCode == 0) {
    "git rev-list --max-parents=0 HEAD failed (exit ${result.exitCode}):\n" +
      result.output.joinToString("\n")
  }
  return result.output.first().trim()
}

/**
 * Returns all commit subject lines between [fromSha] (exclusive) and [toSha] (inclusive) on
 * `develop`, using `git log`.
 */
fun collectCommitsBetween(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  fromSha: String,
  toSha: String
): List<String> {
  val result = commandExecutor.executeCommand(
    workspaceRoot,
    "git", "log", "--oneline", "--no-merges", "$fromSha..$toSha"
  )
  check(result.exitCode == 0) {
    "git log $fromSha..$toSha failed (exit ${result.exitCode}):\n" +
      result.output.joinToString("\n")
  }
  return result.output.filter { it.isNotBlank() }
}

/**
 * Represents a merged pull request extracted from a commit message.
 *
 * @property number the PR number (e.g. 6277)
 * @property title the one-line PR title taken from the commit subject
 */
data class PrEntry(val number: Int, val title: String)

/**
 * Parses PR numbers and titles from [commitLines] by matching GitHub's squash-merge format:
 * `<title> (#<number>)`.
 *
 * @return list of [PrEntry] objects in the order they appear in [commitLines]
 */
fun parsePrEntries(commitLines: List<String>): List<PrEntry> {
  return commitLines.mapNotNull { line ->
    // git log --oneline format: "<short_sha> <subject>"
    val subject = line.substringAfter(" ")
    val match = PR_REFERENCE_REGEX.find(subject) ?: return@mapNotNull null
    val number = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
    // Extract the title as everything before the PR-number reference, so that
    // subjects like "Fix #123: Add feature (#6300)" yield "Fix #123: Add feature"
    // rather than being truncated at the first " (#" occurrence.
    val title = subject.substring(0, match.range.first).trim()
    PrEntry(number = number, title = title)
  }
}

/**
 * Extracts GitHub issue numbers from `Fix #NNNN` / `Fixes #NNNN` patterns in [commitLines].
 *
 * The short SHA prefix present in `git log --oneline` lines is hex-only and can never match
 * these keywords, so stripping it is not necessary.
 *
 * @return deduplicated, sorted list of issue numbers
 */
fun parseFixedIssueNumbers(commitLines: List<String>): List<Int> {
  return commitLines
    .flatMap { FIXES_ISSUE_REGEX.findAll(it).map { m -> m.groupValues[1].toInt() } }
    .toSortedSet()
    .toList()
}

/**
 * Builds the prompt sent to the Vertex AI model for changelog summary generation.
 *
 * @param version the version string being released (e.g. "0.17")
 * @param prListText formatted bullet list of PR titles and numbers
 * @param issueListText formatted bullet list of fixed issue numbers, or "(none)"
 * @return the complete prompt string
 */
fun buildPrompt(version: String, prListText: String, issueListText: String): String {
  return """
    You are writing the changelog for Oppia Android app version $version.
    Oppia is a free educational app helping underserved learners around the world.
    
    Below are the pull requests merged since the previous release:
    $prListText
    
    Referenced issues fixed in this release:
    $issueListText
    
    Write a brief 2-3 sentence summary of this release for end users.
    Focus on user-visible improvements and bug fixes.
    Do not mention pull request numbers, issue numbers, or developer jargon.
    Keep it simple, positive, and friendly.
    IMPORTANT: Your response must be 500 characters or fewer (including spaces and punctuation).
    The Google Play Console release notes field has a 500-character maximum.
  """.trimIndent()
}

/**
 * Calls [vertexAiClient] with [prompt] and returns the summary plus a failure flag.
 *
 * If the call throws any exception, the failure is logged and a fallback raw-list marker is
 * returned instead so the PR can still be created with a placeholder for human review.
 *
 * @return a pair of (summaryText, llmFailed) where [llmFailed] is `true` if the LLM call failed
 */
fun invokeLlmWithFallback(
  vertexAiClient: VertexAiClient,
  prompt: String
): Pair<String, Boolean> {
  return try {
    val summary = vertexAiClient.generateText(prompt)
    println("Vertex AI summary generated successfully.")
    summary to false
  } catch (e: Exception) {
    println("WARNING: Vertex AI call failed — using fallback raw commit list.")
    println("Reason: ${e.message}")
    LLM_FALLBACK_MARKER to true
  }
}

/**
 * Builds the markdown content for `config/changelogs/<version>.md`.
 *
 * If [llmFailed] is `true`, includes [LLM_FALLBACK_MARKER] and a raw list so that a human
 * reviewer can easily replace the placeholder with the actual summary.
 */
fun buildChangelogContent(
  summary: String,
  prEntries: List<PrEntry>,
  issueNumbers: List<Int>,
  llmFailed: Boolean
): String {
  val sb = StringBuilder()
  if (llmFailed) {
    sb.appendLine(LLM_FALLBACK_MARKER)
    sb.appendLine(
      "<!-- Replace the marker above with a 2-3 sentence user-facing summary before release -->"
    )
    sb.appendLine()
    if (prEntries.isNotEmpty()) {
      sb.appendLine("### Changes in this release")
      prEntries.forEach { sb.appendLine("- ${it.title}") }
      sb.appendLine()
    }
    if (issueNumbers.isNotEmpty()) {
      sb.appendLine("### Issues addressed")
      issueNumbers.forEach { sb.appendLine("- #$it") }
    }
  } else {
    sb.appendLine(summary)
  }
  return sb.toString().trimEnd() + "\n"
}

/**
 * Builds the PR description body with links to all reference material.
 *
 * Includes: commit range link, PR list, issue list, and a note on LLM failure if applicable.
 */
fun buildPrBody(
  version: String,
  fromSha: String,
  toSha: String,
  prEntries: List<PrEntry>,
  issueNumbers: List<Int>,
  llmFailed: Boolean
): String {
  val sb = StringBuilder()
  sb.appendLine("## Auto-generated changelog for version $version")
  sb.appendLine()
  if (llmFailed) {
    sb.appendLine(
      "> ⚠️ **LLM generation failed.** The changelog contains a raw commit list. " +
        "Please replace the `$LLM_FALLBACK_MARKER` placeholder with a user-facing summary."
    )
    sb.appendLine()
  }
  sb.appendLine("### Reference material")
  sb.appendLine()
  sb.appendLine(
    "**Commit range:** [`${fromSha.take(7)}`..`${toSha.take(7)}`]" +
      "(https://github.com/$REPO_OWNER/$REPO_NAME/compare/$fromSha...$toSha)"
  )
  sb.appendLine()
  if (prEntries.isNotEmpty()) {
    sb.appendLine("**Pull requests included:**")
    prEntries.forEach { pr ->
      sb.appendLine(
        "- [#${pr.number} — ${pr.title}]" +
          "(https://github.com/$REPO_OWNER/$REPO_NAME/pull/${pr.number})"
      )
    }
    sb.appendLine()
  }
  if (issueNumbers.isNotEmpty()) {
    sb.appendLine("**Issues addressed:**")
    issueNumbers.forEach { n ->
      sb.appendLine(
        "- [#$n](https://github.com/$REPO_OWNER/$REPO_NAME/issues/$n)"
      )
    }
    sb.appendLine()
  }
  sb.appendLine("---")
  sb.appendLine(
    "*This PR was automatically created by `generate_changelogs.yml`. " +
      "Review and merge after verifying the changelog content.*"
  )
  return sb.toString().trimEnd()
}

/**
 * Commits [changelogFile], force-pushes to [branchName], and creates or updates the PR on GitHub.
 *
 * Uses the `gh` CLI (authenticated via the workflow's `GITHUB_TOKEN`) to create the PR. If a PR
 * for [branchName] already exists, it is updated automatically by the force-push.
 */
fun createOrUpdateChangelogPr(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  branchName: String,
  changelogFile: File,
  changelogVersion: String,
  prBody: String
) {
  println("Setting up git config for automated commit...")
  runGit(workspaceRoot, commandExecutor, "config", "user.email", GIT_AUTHOR_EMAIL)
  runGit(workspaceRoot, commandExecutor, "config", "user.name", GIT_AUTHOR_NAME)

  println("Checking out branch $branchName...")
  // Create or reset the branch to HEAD of develop.
  runGitAllowFailure(workspaceRoot, commandExecutor, "branch", "-D", branchName)
  runGit(workspaceRoot, commandExecutor, "checkout", "-b", branchName)

  println("Staging changelog file...")
  runGit(workspaceRoot, commandExecutor, "add", changelogFile.absolutePath)

  println("Committing changelog...")
  runGit(
    workspaceRoot, commandExecutor,
    "commit", "-m", "Add changelog for version $changelogVersion [automated]"
  )

  println("Force-pushing to origin/$branchName...")
  runGit(workspaceRoot, commandExecutor, "push", "--force", "origin", branchName)

  println("Creating or updating PR via gh CLI...")
  val prTitle = "Add changelog for version $changelogVersion"
  val result = commandExecutor.executeCommand(
    workspaceRoot,
    "gh", "pr", "create",
    "--base", "develop",
    "--head", branchName,
    "--title", prTitle,
    "--body", prBody
  )
  if (result.exitCode == 0) {
    val prUrl = result.output.lastOrNull { it.startsWith("https://") } ?: "(URL not found)"
    println("PR created: $prUrl")
  } else {
    // PR already exists — force-push already updated it. Log but don't fail.
    println(
      "gh pr create exited with ${result.exitCode} (PR likely already exists). " +
        "Force-push already updated the branch.\n" +
        result.output.joinToString("\n")
    )
  }
}

private fun runGit(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  vararg args: String
) {
  val result = commandExecutor.executeCommand(workspaceRoot, "git", *args)
  check(result.exitCode == 0) {
    "git ${args.toList()} failed (exit ${result.exitCode}):\n" +
      result.output.joinToString("\n")
  }
}

private fun runGitAllowFailure(
  workspaceRoot: File,
  commandExecutor: CommandExecutor,
  vararg args: String
) {
  commandExecutor.executeCommand(workspaceRoot, "git", *args)
}

private const val CHANGELOGS_DIR = "config/changelogs"
private const val REMOTE = "origin"
private const val DEVELOP_BRANCH = "develop"
private const val REPO_OWNER = "oppia"
private const val REPO_NAME = "oppia-android"
private const val GIT_AUTHOR_EMAIL = "actions@github.com"
private const val GIT_AUTHOR_NAME = "github-actions[bot]"

/** Marker inserted into changelogs when LLM generation fails. */
const val LLM_FALLBACK_MARKER = "<!-- LLM generation failed -->"

/** Matches `MAJOR_VERSION = <n>` in version.bzl. */
private val MAJOR_VERSION_REGEX = Regex("""MAJOR_VERSION\s*=\s*(\d+)""")

/** Matches `MINOR_VERSION = <n>` in version.bzl. */
private val MINOR_VERSION_REGEX = Regex("""MINOR_VERSION\s*=\s*(\d+)""")

/**
 * Matches the `(#<number>)` PR reference at the end of a GitHub squash-merge commit subject.
 * Example: `Fix part of #6106: Add deploy workflow (#6270)` → group 1 = `6270`
 */
private val PR_REFERENCE_REGEX = Regex("""\(#(\d+)\)\s*$""")

/** Matches `Fix #NNNN` / `Fixes #NNNN` patterns in commit messages (case-insensitive). */
private val FIXES_ISSUE_REGEX = Regex("""(?i)fix(?:es)? #(\d+)""")
