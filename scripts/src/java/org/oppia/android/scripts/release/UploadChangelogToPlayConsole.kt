package org.oppia.android.scripts.release

import java.io.File

/**
 * Script that audits live Play Console tracks, detects changelog diffs, and uploads updated
 * release notes when the local `config/changelogs/<version>.md` differs from what is deployed.
 *
 * This script handles *incremental* changelog corrections. The initial changelog is deployed by
 * `UploadBinaryToPlayConsole`; this script only handles post-release updates.
 *
 * Usage (called by deploy_updated_changelog.yml via Bazel):
 * ```
 * bazel run //scripts:upload_changelog_to_play_console -- \
 *   <workspace_path> <package_name> <version> <gcp_project_id>
 * ```
 *
 * @param args[0] workspace_path — absolute path to the repository root
 * @param args[1] package_name — Play Console app package (e.g. "org.oppia.android")
 * @param args[2] version — version in major.minor format matching `config/changelogs/<version>.md`
 * @param args[3] gcp_project_id — GCP project ID for WIF-authenticated `gcloud` token
 */
// Note: PlayConsoleClient and GooglePlayConsoleClient are provided by PR 1.4
// (upload-binary-to-play-console). This file will not compile until that PR merges into develop.
fun main(args: Array<String>) {
  require(args.size == 4) {
    "Usage: upload_changelog_to_play_console <workspace_path> <package_name> <version> " +
      "<gcp_project_id>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspacePath = args[0]
  val packageName = args[1]
  val version = args[2]
  val gcpProjectId = args[3]

  require(workspacePath.isNotBlank()) { "workspace_path must not be blank." }
  require(packageName.isNotBlank()) { "package_name must not be blank." }
  require(gcpProjectId.isNotBlank()) { "gcp_project_id must not be blank." }
  require(version.matches(Regex("""\d+\.\d+"""))) {
    "version must be in major.minor format (e.g. '0.17'), got '$version'."
  }

  val changelogFile = File(workspacePath, "config/changelogs/$version.md")
  require(changelogFile.exists()) {
    "Changelog file not found: ${changelogFile.absolutePath}. " +
      "Expected at config/changelogs/$version.md inside workspace root."
  }

  println("=== Upload Changelog to Play Console ===")
  println("  Package  : $packageName")
  println("  Version  : $version")
  println("  Changelog: ${changelogFile.absolutePath}")
  println()

  // TODO(#PR1.4): Uncomment once PR 1.4 merges.
  // val accessToken = obtainAccessToken(gcpProjectId)
  // val client = GooglePlayConsoleClient(accessToken)
  // val liveTracks = auditLiveTracks(client, packageName)
  // TODO: detect changelog diff and upload (tasks 2 & 3)
}

/**
 * Queries each track in [tracks] and returns a map of track name → live releases.
 *
 * A release is considered **live** if its status is `"completed"` or `"inProgress"`. Tracks with
 * only `"draft"` or `"halted"` releases, or with no releases at all, are excluded from the result.
 *
 * Logs a summary line for each track audited.
 *
 * @param client the [PlayConsoleClient] used to query track state
 * @param packageName the application package name (e.g. `"org.oppia.android"`)
 * @param tracks the list of track names to audit; defaults to all three standard tracks
 * @return a map from track name to its list of live [PlayConsoleClient.TrackRelease] entries,
 *     containing only tracks that have at least one live release
 */
fun auditLiveTracks(
  client: PlayConsoleClient,
  packageName: String,
  tracks: List<String> = AUDITED_TRACKS
): Map<String, List<PlayConsoleClient.TrackRelease>> {
  println("Auditing tracks: $tracks")
  val result = mutableMapOf<String, List<PlayConsoleClient.TrackRelease>>()

  for (track in tracks) {
    val releases = client.getTrackReleases(packageName, track)
    val liveReleases = releases.filter { it.status in LIVE_STATUSES }

    if (liveReleases.isNotEmpty()) {
      println(
        "Track '$track': ${liveReleases.size} live release(s) " +
          "(version codes: ${liveReleases.flatMap { it.versionCodes }})."
      )
      result[track] = liveReleases
    } else {
      println("Track '$track': no live releases — skipping.")
    }
  }

  val liveTrackSummary = result.keys.joinToString(", ").ifEmpty { "none" }
  println("Live tracks found: $liveTrackSummary")
  return result
}

/** Standard Play Console tracks audited by this script. */
private val AUDITED_TRACKS = listOf("alpha", "beta", "production")

/** Release statuses that indicate a build is live (visible to users). */
private val LIVE_STATUSES = setOf("completed", "inProgress")
