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
// Note: PlayConsoleClient and GooglePlayConsoleClient are defined in PR 1.4
// (upload-binary-to-play-console, tracking issue #6106). This file will not compile until
// that PR merges into develop.
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

  val localNotes = changelogFile.readText().trim()

  println("=== Upload Changelog to Play Console ===")
  println("  Package  : $packageName")
  println("  Version  : $version")
  println("  Changelog: ${changelogFile.absolutePath}")
  println("  Notes    : ${localNotes.take(80)}${if (localNotes.length > 80) "..." else ""}")
  println()

  // The wiring below requires two additions from PR 1.4 (tracking issue #6106):
  //   1. obtainAccessToken(gcpProjectId) — currently private to UploadBinaryToPlayConsole.kt;
  //      needs to be extracted to a shared utility (e.g. GcpAuthUtils.kt).
  //   2. PlayConsoleClient.getTrackReleaseNotes(packageName, track) — not yet in the
  //      PlayConsoleClient interface; needed to fetch deployed notes for diff detection.
  //
  // Once both are available this body becomes:
  //
  //   val accessToken = obtainAccessToken(gcpProjectId)
  //   val client = GooglePlayConsoleClient(accessToken)
  //   val liveTracks = auditLiveTracks(client, packageName)
  //
  //   for ((track, releases) in liveTracks) {
  //     val deployedNotes = client.getTrackReleaseNotes(packageName, track)["en-US"].orEmpty()
  //     if (detectChangelogDiff(localNotes, deployedNotes)) {
  //       val versionCode = releases.first().versionCodes.max()
  //       uploadChangelogToTrack(client, packageName, track, versionCode, mapOf("en-US" to localNotes))
  //     }
  //   }
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

/**
 * Determines whether the Play Console release notes for a track need to be updated.
 *
 * Compares [localNotes] (read from `config/changelogs/`) against [deployedNotes] (fetched from
 * the Play Developer API). Both are trimmed before comparison so trailing whitespace and newlines
 * do not trigger spurious updates.
 *
 * @param localNotes the changelog content from the repository (`config/changelogs/<version>.md`)
 * @param deployedNotes the `en-US` release notes currently live on the Play Console track
 * @return `true` if the notes differ and an upload should be performed; `false` if they are
 *     already in sync
 */
fun detectChangelogDiff(localNotes: String, deployedNotes: String): Boolean {
  val trimmedLocal = localNotes.trim()
  val trimmedDeployed = deployedNotes.trim()

  return if (trimmedLocal == trimmedDeployed) {
    println("Changelog is already up to date on this track — no upload needed.")
    false
  } else {
    val deployedSnippet = trimmedDeployed.take(60) + if (trimmedDeployed.length > 60) "..." else ""
    val localSnippet = trimmedLocal.take(60) + if (trimmedLocal.length > 60) "..." else ""
    println(
      "Changelog diff detected:" +
        "\n  deployed : $deployedSnippet" +
        "\n  local    : $localSnippet"
    )
    true
  }
}

/**
 * Uploads updated release notes to a live Play Console track within a new edit session.
 *
 * Creates a new edit, updates the release notes for the highest live version code on [track], and
 * commits the edit. This is a *changelog-only* update — the binary itself is not changed.
 *
 * **Precondition:** [liveTracks] must contain [track] as a key (i.e., the track must have at
 * least one live release). Call [auditLiveTracks] first and only call this function for tracks
 * present in its result.
 *
 * @param client the [PlayConsoleClient] used for all API calls
 * @param packageName the application package name (e.g. `"org.oppia.android"`)
 * @param track the Play Console track to update (e.g. `"alpha"`, `"beta"`, `"production"`)
 * @param versionCode the version code of the live release to attach the updated notes to
 * @param newNotes map of BCP-47 language codes to updated release notes text (max 500 chars each);
 *     must contain at least an `"en-US"` entry
 * @throws IllegalStateException if [newNotes] is empty or has no `"en-US"` entry
 */
fun uploadChangelogToTrack(
  client: PlayConsoleClient,
  packageName: String,
  track: String,
  versionCode: Long,
  newNotes: Map<String, String>
) {
  require(newNotes.containsKey("en-US")) {
    "newNotes must contain an 'en-US' entry. Got keys: ${newNotes.keys}"
  }
  require(newNotes.values.all { it.length <= MAX_RELEASE_NOTES_LENGTH }) {
    "Release notes must not exceed $MAX_RELEASE_NOTES_LENGTH characters. " +
      "Longest entry: ${newNotes.values.maxOf { it.length }} chars."
  }

  println("Uploading updated changelog to track '$track' (version code: $versionCode)...")

  val editId = client.createEdit(packageName)
  println("  Edit session: $editId")

  // Changelog-only updates do not change the binary; use a full rollout (1.0) so the track
  // assignment is preserved as-is. For in-progress (staged) releases, the fraction is kept at
  // 100% since this script only updates text, not the user rollout percentage.
  client.setTrackRelease(packageName, editId, track, versionCode, 1.0, newNotes)
  println("  Track release notes updated.")

  client.commitEdit(packageName, editId)
  println("  Edit committed. Track '$track' release notes are now live.")
}

/** Standard Play Console tracks audited by this script. */
private val AUDITED_TRACKS = listOf("alpha", "beta", "production")

/** Release statuses that indicate a build is live (visible to users). */
private val LIVE_STATUSES = setOf("completed", "inProgress")

/** Maximum length of release notes accepted by the Play Developer API. */
private const val MAX_RELEASE_NOTES_LENGTH = 500
