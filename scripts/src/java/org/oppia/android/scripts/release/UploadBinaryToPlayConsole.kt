package org.oppia.android.scripts.release

import java.io.File

/**
 * Main entry point for uploading a signed Android App Bundle (AAB) to the Google Play Console.
 *
 * This script:
 * 1. Validates all inputs.
 * 2. Runs pre-upload precondition checks: pending release detection and changelog existence. These
 *    checks do not require the version code and are safe to run before any mutations.
 * 3. Opens a Play Console edit session and uploads the AAB. The version code is returned by the
 *    API after upload and is used for the version inversion check (step 4).
 * 4. Runs the version inversion check using the uploaded version code. If the check fails the edit
 *    session is abandoned (never committed) and no change is published.
 * 5. Assigns the binary to the target track and commits the edit.
 *
 * Release notes are sourced from [CHANGELOGS_DIR]: a flavor-specific file
 * (`<major.minor>_<flavor>.md`) is used if it exists, otherwise the default (`<major.minor>.md`)
 * is used.
 *
 * Usage (called by deploy_to_play_console.yml via Bazel):
 * ```
 * bazel run //scripts:upload_binary_to_play_console -- \
 *   <workspace_root> <aab_path> <track> <gcp_project_id> <rollout_fraction>
 * ```
 *
 * @param args[0] workspace_root — absolute path to the repository root
 * @param args[1] aab_path — absolute path to the signed AAB to upload
 * @param args[2] track — Play Console track: "alpha", "beta", or "production"
 * @param args[3] gcp_project_id — GCP project ID used to obtain an access token via gcloud
 * @param args[4] rollout_fraction — staged rollout fraction between 0.0 and 1.0
 */
fun main(args: Array<String>) {
  require(args.size == 5) {
    "Usage: upload_binary_to_play_console <workspace_root> <aab_path> <track> " +
      "<gcp_project_id> <rollout_fraction>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspaceRoot = args[0]
  val aabPath = args[1]
  val track = args[2]
  val gcpProjectId = args[3]
  val rolloutFraction = requireNotNull(args[4].toDoubleOrNull()) {
    "rollout_fraction must be a valid decimal number (0.0-1.0), got '${args[4]}'."
  }

  require(track in VALID_TRACKS) {
    "track must be one of $VALID_TRACKS, got '$track'."
  }
  require(rolloutFraction in 0.0..1.0) {
    "rollout_fraction must be between 0.0 and 1.0, got $rolloutFraction."
  }

  val aabFile = File(aabPath)
  require(aabFile.exists()) { "AAB file not found: $aabPath" }

  val aabName = aabFile.name
  val versionName = extractVersionName(aabName)
    ?: error(
      "Cannot extract version name from AAB filename '$aabName'. " +
        "Expected format: oppia-android-<major>.<minor>-rc<rc>-<flavor>-<hash>.aab"
    )
  val majorMinorVersion = extractMajorMinorVersion(versionName)
    ?: error("Cannot extract major.minor version from version name '$versionName'.")
  val flavor = extractFlavor(aabName)
    ?: error("Cannot extract flavor from AAB filename '$aabName'.")

  println("=== Upload Binary to Play Console ===")
  println("  AAB     : $aabName")
  println("  Version : $versionName (major.minor: $majorMinorVersion)")
  println("  Flavor  : $flavor")
  println("  Track   : $track")
  println("  Rollout : ${rolloutFraction * 100}%")
  println()

  val accessToken = obtainAccessToken(gcpProjectId)
  val client = GooglePlayConsoleClient(accessToken)
  runUpload(
    client, workspaceRoot, aabPath, versionName, majorMinorVersion, flavor, track, rolloutFraction
  )
}

/**
 * Executes the full upload workflow after authentication.
 *
 * Extracted to allow unit tests to inject a [PlayConsoleClient] fake, bypassing `gcloud`.
 *
 * @param client the [PlayConsoleClient] used for all Play Console API calls
 * @param workspaceRoot absolute path to the repository root (for changelog lookups)
 * @param aabPath absolute path to the signed AAB to upload
 * @param versionName the full version string (e.g. "0.18-rc01-alpha")
 * @param majorMinorVersion the `major.minor` portion used for changelog file lookup
 * @param flavor the build flavor ("alpha", "beta", or "ga")
 * @param track the Play Console track ("alpha", "beta", or "production")
 * @param rolloutFraction the fraction of users to roll out to, between 0.0 and 1.0 inclusive.
 *     Passed directly to [PlayConsoleClient.setTrackRelease].
 */
fun runUpload(
  client: PlayConsoleClient,
  workspaceRoot: String,
  aabPath: String,
  versionName: String,
  majorMinorVersion: String,
  flavor: String,
  track: String,
  rolloutFraction: Double
) {
  // Pre-upload checks that don't require the version code.
  println("Running pre-upload precondition checks...")
  PendingReleaseCheck(client).verify(PACKAGE_NAME, track)
  ChangelogExistenceCheck(workspaceRoot).verify(majorMinorVersion, flavor)
  println("Pre-upload checks passed.\n")

  // Upload the AAB. The version code is assigned by the Play Console and returned here.
  println("Opening edit session...")
  val editId = client.createEdit(PACKAGE_NAME)
  println("Edit session: $editId")

  println("Uploading AAB...")
  val uploadedVersionCode = client.uploadAab(PACKAGE_NAME, editId, aabPath)
  println("Uploaded — assigned version code: $uploadedVersionCode")

  // Version inversion check runs after upload so we have the actual version code from the API.
  // If this check fails, the edit session is abandoned (not committed) — nothing is published.
  println("Running version inversion check...")
  VersionInversionCheck(client).verify(PACKAGE_NAME, track, uploadedVersionCode)
  println("Version inversion check passed.\n")

  val releaseNotes = extractReleaseNotes(workspaceRoot, majorMinorVersion, flavor)
  client.setTrackRelease(
    PACKAGE_NAME, editId, track, uploadedVersionCode, rolloutFraction, releaseNotes
  )
  println("Track '$track' updated.")

  client.commitEdit(PACKAGE_NAME, editId)
  println(
    "\nDone: $versionName (vc=$uploadedVersionCode) committed to Play Console track '$track'."
  )
}

/**
 * Obtains an OAuth2 access token for the Google Play Developer API via
 * `gcloud auth print-access-token`.
 *
 * In CI this uses Application Default Credentials set up by Workload Identity Federation.
 * Locally, `gcloud auth application-default login` must have been run first.
 */
private fun obtainAccessToken(gcpProjectId: String): String {
  val process = ProcessBuilder(
    "gcloud", "auth", "print-access-token", "--project=$gcpProjectId"
  )
    .redirectErrorStream(true)
    .start()
  val output = process.inputStream.bufferedReader().readText().trim()
  val exitCode = process.waitFor()
  check(exitCode == 0) {
    "Failed to obtain access token (exit code $exitCode):\n$output"
  }
  return output
}

/**
 * Extracts the full version name from an AAB filename.
 *
 * Example: `oppia-android-0.18-rc01-alpha-e740815230.aab` → `0.18-rc01-alpha`
 */
private fun extractVersionName(aabName: String): String? {
  val pattern = Regex("""oppia-android-(\d+\.\d+-rc\d+-(?:alpha|beta|ga))-[0-9a-f]+\.aab""")
  return pattern.find(aabName)?.groupValues?.get(1)
}

/**
 * Extracts the major.minor portion of the version name for changelog lookups.
 *
 * Example: `0.18-rc01-alpha` → `0.18`
 */
private fun extractMajorMinorVersion(versionName: String): String? {
  val pattern = Regex("""^(\d+\.\d+)-rc\d+-(?:alpha|beta|ga)$""")
  return pattern.find(versionName)?.groupValues?.get(1)
}

/**
 * Extracts the build flavor from an AAB filename.
 *
 * Example: `oppia-android-0.18-rc01-alpha-e740815230.aab` → `alpha`
 */
private fun extractFlavor(aabName: String): String? {
  val pattern = Regex("""oppia-android-\d+\.\d+-rc\d+-(alpha|beta|ga)-[0-9a-f]+\.aab""")
  return pattern.find(aabName)?.groupValues?.get(1)
}

/**
 * Reads the release notes for [majorMinorVersion] and [flavor] from [CHANGELOGS_DIR].
 *
 * Lookup order:
 * 1. `config/changelogs/<majorMinorVersion>_<flavor>.md` (flavor-specific override)
 * 2. `config/changelogs/<majorMinorVersion>.md` (default)
 *
 * Returns a map with a single "en-US" entry. Returns an empty map if no file is found (the
 * changelog check will have already caught this case before any upload is attempted).
 */
fun extractReleaseNotes(
  workspaceRoot: String,
  majorMinorVersion: String,
  flavor: String
): Map<String, String> {
  val changelogsDir = File(workspaceRoot, CHANGELOGS_DIR)
  val flavorSpecificFile = File(changelogsDir, "${majorMinorVersion}_$flavor.md")
  val defaultFile = File(changelogsDir, "$majorMinorVersion.md")

  val changelogFile = when {
    flavorSpecificFile.exists() -> flavorSpecificFile
    defaultFile.exists() -> defaultFile
    else -> return emptyMap()
  }

  val notes = changelogFile.readText().trim().take(MAX_RELEASE_NOTES_LENGTH)
  return if (notes.isEmpty()) emptyMap() else mapOf("en-US" to notes)
}

private const val PACKAGE_NAME = "org.oppia.android"
private const val CHANGELOGS_DIR = "config/changelogs"
private const val MAX_RELEASE_NOTES_LENGTH = 500
private val VALID_TRACKS = setOf("alpha", "beta", "production")
