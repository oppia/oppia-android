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
 * bazel run //scripts:upload_changelog_to_play_console -- \
 *   <workspace_path> <package_name> <version> <gcp_access_token>
 * ```
 *
 * Arguments (positional):
 *   0. workspace_path   — absolute path to the repository root
 *   1. package_name     — Play Console app package (e.g. "org.oppia.android")
 *   2. version          — version in major.minor format (e.g. "0.17"), used to locate
 *       `config/changelogs/<version>[_<track>].md`
 *   3. gcp_access_token — OAuth2 bearer token; obtain via `gcloud auth print-access-token`
 *
 * Example:
 * ```
 * bazel run //scripts:upload_changelog_to_play_console -- \
 *   "$(pwd)" org.oppia.android 0.18 "$(gcloud auth print-access-token)"
 * ```
 *
 * An optional 5th argument overrides the API base URL. This is used in tests to route all
 * Play Console HTTP calls through a local MockWebServer instead of the real endpoint.
 */
fun main(args: Array<String>) {
  require(args.size in 4..5) {
    "Usage: upload_changelog_to_play_console <workspace_path> <package_name> <version> " +
      "<gcp_access_token>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspacePath = args[0]
  val packageName = args[1]
  val version = args[2]
  val gcpAccessToken = args[3]
  val apiBaseUrl = args.getOrNull(4) ?: GooglePlayConsoleClient.PRODUCTION_API_BASE_URL

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

  val isAutomaticRun = System.getenv("IS_AUTOMATIC_RUN")?.toBooleanStrictOrNull() == true ||
    System.getenv("GITHUB_EVENT_NAME") == "push"

  val client = GooglePlayConsoleClient(gcpAccessToken, apiBaseUrl)
  maybeUploadUpdatedChangelogs(client, workspacePath, packageName, version, isAutomaticRun)
}

/**
 * Audits all live Play Console tracks and uploads updated release notes for [version] to each
 * track that has a corresponding changelog file in `config/changelogs/`.
 *
 * Verifies authoritatively that [version] is currently live on at least one Play Console track
 * before creating or committing an edit session. If [version] is not live:
 * - If [isAutomaticRun] is `true`, logs a message and returns safely without opening an edit.
 * - If [isAutomaticRun] is `false` (manual trigger), throws [IllegalStateException] with a clear message.
 *
 * For tracks with an active release matching [version], release notes are resolved and uploaded.
 * Tracks whose live release does not match [version] or with no matching changelog file are skipped.
 *
 * @param client the [PlayConsoleClient] used for all Play Console API calls
 * @param workspacePath absolute path to the repository root (for changelog lookups)
 * @param packageName the application package name (e.g. `"org.oppia.android"`)
 * @param version version in major.minor format (e.g. `"0.17"`)
 * @param isAutomaticRun whether this run was triggered automatically (e.g. on push) vs. manually
 * @param frozenVersionCodesPerTrack frozen codes to preserve; defaults to the release config
 */
@JvmOverloads
fun maybeUploadUpdatedChangelogs(
  client: PlayConsoleClient,
  workspacePath: String,
  packageName: String,
  version: String,
  isAutomaticRun: Boolean = false,
  frozenVersionCodesPerTrack: Map<String, Set<Long>> = FROZEN_VERSION_CODES_PER_TRACK
) {
  val liveTracks = auditLiveTracks(client, packageName)

  if (liveTracks.isEmpty()) {
    if (isAutomaticRun) {
      println("No live tracks found — nothing to update.")
      return
    } else {
      error(
        "No live tracks found for '$packageName'. Confirm that a release has been deployed " +
          "before running a manual changelog upload."
      )
    }
  }

  // Authoritative version validation: identify tracks where the active non-frozen release
  // matches the target version.
  val matchingTracks = liveTracks.filter { (track, releases) ->
    val frozenCodes = frozenVersionCodesPerTrack[track] ?: emptySet()
    val activeRelease = findHighestNonFrozenVersionCode(releases, frozenCodes)?.first
    activeRelease != null && releaseMatchesVersion(activeRelease, version)
  }

  for ((track, releases) in liveTracks) {
    if (track !in matchingTracks) {
      val frozenCodes = frozenVersionCodesPerTrack[track] ?: emptySet()
      val activeRelease = findHighestNonFrozenVersionCode(releases, frozenCodes)?.first
      val releaseLabel =
        activeRelease?.name ?: activeRelease?.versionCodes?.toString() ?: "unknown"
      println(
        "Track '$track': live release ($releaseLabel) does not match target version " +
          "$version — skipping."
      )
    }
  }

  if (matchingTracks.isEmpty()) {
    if (isAutomaticRun) {
      println(
        "Target version $version is not currently live on any Play Console track — " +
          "skipping automatic changelog upload."
      )
      return
    } else {
      error(
        "Target version '$version' is not currently live on any Play Console track. " +
          "Confirm that the release has been deployed before running a manual changelog upload."
      )
    }
  }

  var updatedCount = 0
  for ((track, releases) in matchingTracks) {
    val notes = resolveNotesForTrack(workspacePath, version, track)
    if (notes.isEmpty()) {
      println("Track '$track': no changelog file found for version $version — skipping.")
      continue
    }
    val frozenVersionCodes = frozenVersionCodesPerTrack[track] ?: emptySet()
    val (selectedRelease, versionCode) = checkNotNull(
      findHighestNonFrozenVersionCode(releases, frozenVersionCodes)
    ) {
      "Track '$track' has no version codes outside its frozen builds — cannot update its changelog."
    }
    // Preserve the rollout of the release containing the selected version code. Completed
    // releases are already at 100%, so fall back to 1000 when they have no rollout permille.
    val rolloutPermille = selectedRelease.rolloutPermille ?: 1000
    if (frozenVersionCodes.isNotEmpty()) {
      val liveVersionCodes = releases.flatMap { it.versionCodes }.toSet()
      val missingFrozen = frozenVersionCodes - liveVersionCodes
      check(missingFrozen.isEmpty()) {
        "Invariant disruption: frozen version code(s) $missingFrozen expected on track '$track' " +
          "are missing from the live track. This indicates a major release state inconsistency."
      }
    }
    // Skip if the live notes already match the local file — avoids wasted API calls and
    // prevents spurious review re-triggers on tracks where nothing has changed.
    val deployedNotes = selectedRelease.releaseNotes["en-US"] ?: ""
    if (!detectChangelogDiff(notes["en-US"] ?: "", deployedNotes)) continue
    uploadChangelogToTrack(
      client, packageName, track, versionCode, rolloutPermille, notes, frozenVersionCodes.toList()
    )
    updatedCount++
  }

  println()
  println("Changelog upload complete: $updatedCount track(s) updated")
}

/**
 * Determines whether [release] matches the requested [version] (e.g. "0.18" or "0.9").
 *
 * Checks that [release.name] matches [version] or starts with [version] followed by a standard
 * delimiter (`-`, `_`, `.`, `+`, space, `(`, etc.), allowing names like "0.18", "0.18-rc01-alpha",
 * "v0.18", "0.18.0", and "0.18_alpha", while ensuring "0.18" never matches "0.180" or "0.19".
 *
 * For single-digit minor versions (e.g. "0.9"), both unpadded ("0.9") and zero-padded ("0.09")
 * formats are matched to support both modern and historical Oppia Android versionName formats.
 */
fun releaseMatchesVersion(release: PlayConsoleClient.TrackRelease, version: String): Boolean {
  val releaseName = release.name?.removePrefix("v")?.removePrefix("V")?.trim() ?: return false
  val candidatePrefixes = getCandidateVersionPrefixes(version)
  return candidatePrefixes.any { prefix ->
    releaseName == prefix ||
      releaseName.startsWith("$prefix-") ||
      releaseName.startsWith("${prefix}_") ||
      releaseName.startsWith("$prefix.") ||
      releaseName.startsWith("$prefix+") ||
      releaseName.startsWith("$prefix ") ||
      releaseName.startsWith("$prefix(")
  }
}

/**
 * Returns candidate version strings for [version] to handle both unpadded (e.g. "0.9") and
 * zero-padded (e.g. "0.09") minor version numbering across different release eras.
 */
private fun getCandidateVersionPrefixes(version: String): Set<String> {
  val match = Regex("""^(\d+)\.(\d+)$""").matchEntire(version.trim()) ?: return setOf(version)
  val major = match.groupValues[1]
  val minorInt = match.groupValues[2].toIntOrNull() ?: return setOf(version)
  val unpadded = "$major.$minorInt"
  val padded = "$major.%02d".format(minorInt)
  return setOf(unpadded, padded)
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
private fun auditLiveTracks(
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
private fun detectChangelogDiff(localNotes: String, deployedNotes: String): Boolean {
  val trimmedLocal = localNotes.trim()
  val trimmedDeployed = deployedNotes.trim()

  return if (trimmedLocal == trimmedDeployed) {
    println("Changelog is already up to date on this track — no upload needed.")
    false
  } else {
    println("Changelog diff detected. Currently deployed notes:\n$trimmedDeployed")
    true
  }
}

/**
 * Uploads updated release notes to a live Play Console track within a new edit session.
 *
 * Creates a new edit, updates the release notes for [versionCode] on [track], and commits the
 * edit. This is a *changelog-only* update -- the binary itself is not changed. The caller must
 * pass the existing [rolloutPermille] from the live release to avoid inadvertently altering the
 * staged rollout percentage.
 *
 * Any [frozenVersionCodes] are merged into the new release's versionCodes list so that the
 * Play Console API does not deactivate them during the track update.
 *
 * @param client the [PlayConsoleClient] used for all API calls
 * @param packageName the application package name (e.g. `"org.oppia.android"`)
 * @param track the Play Console track to update (e.g. `"alpha"`, `"beta"`, `"production"`)
 * @param versionCode the version code of the live release to attach the updated notes to
 * @param rolloutPermille the existing staged rollout permille from the live release (passed
 *     through unchanged so the rollout percentage is preserved)
 * @param newNotes map of BCP-47 language codes to updated release notes text (max 500 chars each);
 *     must contain at least an `"en-US"` entry
 * @param frozenVersionCodes frozen OS-specific version codes to merge into the new release entry
 *     alongside [versionCode] to prevent them being deactivated by the track update
 */
private fun uploadChangelogToTrack(
  client: PlayConsoleClient,
  packageName: String,
  track: String,
  versionCode: Long,
  rolloutPermille: Int,
  newNotes: Map<String, String>,
  frozenVersionCodes: List<Long> = emptyList()
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

  client.setTrackRelease(
    packageName, editId, track, versionCode, rolloutPermille, newNotes, frozenVersionCodes
  )
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
private fun resolveNotesForTrack(
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
