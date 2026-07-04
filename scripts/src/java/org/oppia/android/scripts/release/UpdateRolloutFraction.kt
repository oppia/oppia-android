package org.oppia.android.scripts.release

import java.io.File

/**
 * Script that updates the staged rollout fraction for a live release on a single Play Console
 * track, without re-uploading the binary.
 *
 * This is the correct way to increase (or decrease) a staged rollout after the initial binary
 * deployment. Re-uploading the AAB to change the rollout fraction is wasteful and can introduce
 * unintended changes; this script performs a rollout-only edit via the Play Developer API.
 *
 * The release notes for [version] are read from `config/changelogs/` (same lookup order as
 * `UploadChangelogToPlayConsole`) and passed through unchanged so the notes are preserved.
 *
 * Usage (called by update_rollout.yml via Bazel):
 * ```
 * bazel run //scripts:update_rollout_fraction -- \
 *   <workspace_path> <package_name> <track> <version> <rollout_fraction> <gcp_access_token>
 * ```
 *
 * Arguments (positional):
 *   0. workspace_path   — absolute path to the repository root
 *   1. package_name     — Play Console app package (e.g. "org.oppia.android")
 *   2. track            — Play Console track: "alpha", "beta", or "production"
 *   3. version          — version in major.minor format (e.g. "0.17")
 *   4. rollout_fraction — new rollout as an integer in [0, 1000] (e.g. 500 = 50%, 1000 = 100%)
 *   5. gcp_access_token — OAuth2 bearer token; obtain via `gcloud auth print-access-token`
 *
 * An optional 7th argument overrides the API base URL. This is used in tests to route all
 * Play Console HTTP calls through a local MockWebServer instead of the real endpoint.
 */
fun main(args: Array<String>) {
  require(args.size in 6..7) {
    "Usage: update_rollout_fraction <workspace_path> <package_name> <track> <version> " +
      "<rollout_fraction> <gcp_access_token>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspacePath = args[0]
  val packageName = args[1]
  val track = args[2]
  val version = args[3]
  val rolloutFraction = requireNotNull(args[4].toIntOrNull()) {
    "rollout_fraction must be an integer in [0, 1000] (e.g. 500 for 50%), got '${args[4]}'."
  }
  val gcpAccessToken = args[5]
  val apiBaseUrl = args.getOrNull(6) ?: GooglePlayConsoleClient.PRODUCTION_API_BASE_URL

  require(workspacePath.isNotBlank()) { "workspace_path must not be blank." }
  require(packageName.isNotBlank()) { "package_name must not be blank." }
  require(track in VALID_TRACKS) { "track must be one of $VALID_TRACKS, got '$track'." }
  require(version.matches(Regex("""\d+\.\d+"""))) {
    "version must be in major.minor format (e.g. '0.17'), got '$version'."
  }
  require(rolloutFraction in 0..1000) {
    "rollout_fraction must be between 0 and 1000, got $rolloutFraction."
  }
  require(gcpAccessToken.isNotBlank()) { "gcp_access_token must not be blank." }

  println("=== Update Rollout Fraction ===")
  println("  Package  : $packageName")
  println("  Track    : $track")
  println("  Version  : $version")
  println("  Rollout  : ${rolloutFraction / 10.0}%")
  println()

  val client = GooglePlayConsoleClient(gcpAccessToken, apiBaseUrl)
  updateRollout(client, workspacePath, packageName, track, version, rolloutFraction)
}

/**
 * Executes the rollout fraction update workflow.
 *
 * Verifies that [track] has a live release, reads the current release notes from
 * `config/changelogs/` for [version], and updates the rollout fraction via a new Play Console
 * edit session. The release notes are read from the local file and passed through unchanged so
 * they are preserved; only the rollout fraction is updated.
 *
 * @param client the [PlayConsoleClient] used for all Play Console API calls
 * @param workspacePath absolute path to the repository root (for changelog lookups)
 * @param packageName the application package name (e.g. `"org.oppia.android"`)
 * @param track the Play Console track to update (e.g. `"alpha"`, `"beta"`, `"production"`)
 * @param version version in major.minor format (e.g. `"0.17"`)
 * @param rolloutFraction the new staged rollout fraction as an integer in [0, 1000]
 */
fun updateRollout(
  client: PlayConsoleClient,
  workspacePath: String,
  packageName: String,
  track: String,
  version: String,
  rolloutFraction: Int
) {
  val liveReleases = client.getTrackReleases(packageName, track)
    .filter { it.status in LIVE_STATUSES }

  check(liveReleases.isNotEmpty()) {
    "Track '$track' has no live releases — cannot update rollout fraction."
  }

  val versionCode = checkNotNull(liveReleases.flatMap { it.versionCodes }.maxOrNull()) {
    "Track '$track' has live releases but no version codes — this should not happen."
  }

  println("Live release found on '$track': version code $versionCode.")

  val releaseNotes = resolveReleaseNotes(workspacePath, version, track)
  if (releaseNotes.isEmpty()) {
    println(
      "Warning: no changelog file found for version $version on track '$track' — " +
        "release notes will be cleared by this update."
    )
  }

  println("Updating rollout fraction to ${rolloutFraction / 10.0}%...")

  val editId = client.createEdit(packageName)
  println("  Edit session: $editId")

  client.setTrackRelease(packageName, editId, track, versionCode, rolloutFraction, releaseNotes)
  println("  Rollout fraction updated.")

  client.commitEdit(packageName, editId)
  println("  Edit committed. Track '$track' rollout is now ${rolloutFraction / 10.0}%.")
}

/**
 * Resolves the release notes for [track] at [version] from the local changelog directory.
 *
 * Lookup order:
 * 1. `config/changelogs/<version>_<track>.md` (track-specific override)
 * 2. `config/changelogs/<version>.md` (shared fallback)
 *
 * Returns an empty map if neither file exists or the file is empty.
 *
 * @param workspacePath absolute path to the repository root
 * @param version version in major.minor format (e.g. `"0.17"`)
 * @param track Play Console track name (e.g. `"alpha"`, `"beta"`, `"production"`)
 * @return map with `"en-US"` key to notes text, or empty map if no file found
 * @throws IllegalStateException if the resolved file exceeds [MAX_RELEASE_NOTES_LENGTH] characters
 */
private fun resolveReleaseNotes(
  workspacePath: String,
  version: String,
  track: String
): Map<String, String> {
  val changelogsDir = File(workspacePath, CHANGELOGS_DIR)
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

/** Valid Play Console tracks for rollout updates. */
private val VALID_TRACKS = setOf("alpha", "beta", "production")

/** Release statuses that indicate a build is live and eligible for a rollout update. */
private val LIVE_STATUSES = setOf("completed", "inProgress")

/** Maximum length of release notes accepted by the Play Developer API. */
private const val MAX_RELEASE_NOTES_LENGTH = 500

/** Relative path within the workspace root where changelog files are stored. */
private const val CHANGELOGS_DIR = "config/changelogs"
