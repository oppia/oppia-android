package org.oppia.android.scripts.release

import java.io.File

/**
 * Script that audits live Play Console tracks and uploads updated release notes when the local
 * `config/changelogs/<version>[_<track>].md` differs from the deployed text.
 *
 * This script handles *incremental* changelog corrections. The initial changelog is deployed by
 * `UploadBinaryToPlayConsole`; this script only handles post-release updates.
 *
 * **Changelog file resolution (per track):**
 * For each live track the script first looks for a track-specific file
 * (`config/changelogs/<version>_<track>.md`). If that does not exist it falls back to the shared
 * file (`config/changelogs/<version>.md`). Tracks with no matching file are skipped silently,
 * so the shared file can serve as the common changelog while individual tracks can override it.
 *
 * Usage (called by deploy_updated_changelog.yml via Bazel):
 * ```
 * bazel run //scripts:upload_changelog_to_play_console -- \\
 *   <workspace_path> <package_name> <version> <gcp_access_token>
 * ```
 *
 * @param args[0] workspace_path — absolute path to the repository root
 * @param args[1] package_name — Play Console app package (e.g. "org.oppia.android")
 * @param args[2] version — version in major.minor format (e.g. "0.17"), used to locate
 *     `config/changelogs/<version>[_<track>].md`
 * @param args[3] gcp_access_token — OAuth2 access token; obtain via
 *     `gcloud auth print-access-token`
 */
// Note: PlayConsoleClient and GooglePlayConsoleClient are defined in PR 1.4
// (upload-binary-to-play-console, tracking issue #6106). This file will not compile until
// that PR merges into develop.
fun main(args: Array<String>) {
  require(args.size == 4) {
    "Usage: upload_changelog_to_play_console <workspace_path> <package_name> <version> " +
      "<gcp_access_token>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspacePath = args[0]
  val packageName = args[1]
  val version = args[2]
  val gcpAccessToken = args[3]

  require(workspacePath.isNotBlank()) { "workspace_path must not be blank." }
  require(packageName.isNotBlank()) { "package_name must not be blank." }
  require(gcpAccessToken.isNotBlank()) { "gcp_access_token must not be blank." }
  require(version.matches(Regex("""\d+\.\d+"""))) {
    "version must be in major.minor format (e.g. '0.17'), got '$version'."
  }

  println("=== Upload Changelog to Play Console ===")
  println("  Package : $packageName")
  println("  Version : $version")
  println()

  val client = GooglePlayConsoleClient(gcpAccessToken)
  val liveTracks = auditLiveTracks(client, packageName)

  if (liveTracks.isEmpty()) {
    println("No live tracks found — nothing to update.")
    return
  }

  var updatedCount = 0
  for ((track, releases) in liveTracks) {
    val notes = resolveNotesForTrack(workspacePath, version, track)
    if (notes.isEmpty()) {
      println("Track '$track': no changelog file found for version $version — skipping.")
      continue
    }
    val versionCode = releases.flatMap { it.versionCodes }.maxOrNull()!!
    uploadChangelogToTrack(client, packageName, track, versionCode, notes)
    updatedCount++
  }

  println()
  println("Changelog upload complete: $updatedCount track(s) updated")
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

  // Changelog-only updates do not change the binary; use a full rollout (1000 = 100%) so the
  // track assignment is preserved as-is.
  client.setTrackRelease(packageName, editId, track, versionCode, 1000, newNotes)
  println("  Track release notes updated.")

  client.commitEdit(packageName, editId)
  println("  Edit committed. Track '$track' release notes are now live.")
}

/**
 * Resolves the release notes for [track] at [version] using the standard lookup order:
 * 1. `config/changelogs/<version>_<track>.md` (track-specific override)
 * 2. `config/changelogs/<version>.md` (shared fallback)
 *
 * Returns an empty map if neither file exists or the resolved file is empty.
 *
 * @param workspaceRoot absolute path to the repository root
 * @param version version in major.minor format (e.g. `"0.18"`)
 * @param track Play Console track name (e.g. `"alpha"`, `"beta"`, `"production"`)
 * @return map with `"en-US"` key to notes text, or empty map if no file found
 * @throws IllegalStateException if the resolved file exceeds [MAX_RELEASE_NOTES_LENGTH] characters
 */
fun resolveNotesForTrack(
  workspaceRoot: String,
  version: String,
  track: String
): Map<String, String> {
  val changelogsDir = File(workspaceRoot, CHANGELOGS_DIR)
  val trackSpecificFile = File(changelogsDir, "${version}_$track.md")
  val sharedFile = File(changelogsDir, "$version.md")

  val changelogFile = when {
    trackSpecificFile.exists() -> trackSpecificFile
    sharedFile.exists() -> sharedFile
    else -> return emptyMap()
  }

  val notes = changelogFile.readText().trim()
  check(notes.length <= MAX_RELEASE_NOTES_LENGTH) {
    "Changelog '${changelogFile.name}' exceeds the $MAX_RELEASE_NOTES_LENGTH character " +
      "limit (${notes.length} chars). Trim it before deploying."
  }
  return if (notes.isEmpty()) emptyMap() else mapOf("en-US" to notes)
}

/** Standard Play Console tracks audited by this script. */
private val AUDITED_TRACKS = listOf("alpha", "beta", "production")

/** Release statuses that indicate a build is live (visible to users). */
private val LIVE_STATUSES = setOf("completed", "inProgress")

/** Maximum length of release notes accepted by the Play Developer API. */
private const val MAX_RELEASE_NOTES_LENGTH = 500

/** Relative path within the workspace root where changelog files are stored. */
private const val CHANGELOGS_DIR = "config/changelogs"
