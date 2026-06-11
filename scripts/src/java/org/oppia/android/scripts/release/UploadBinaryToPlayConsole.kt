package org.oppia.android.scripts.release

import java.io.File

/**
 * Main entry point for uploading a signed Android App Bundle (AAB) to the Google Play Console.
 *
 * This script:
 * 1. Validates all inputs.
 * 2. Runs precondition checks: pending release detection, version inversion, and changelog
 *    existence. Any failure halts the upload before any API mutations are made.
 * 3. Opens a Play Console edit session, uploads the AAB, assigns it to the target track, and
 *    commits the edit.
 *
 * Release notes are extracted from [CHANGELOG_FILE] for the given version. If no entry is found
 * the changelog check (step 2) will already have failed.
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
        "Expected format: oppia-android-<version>-<flavor>-<hash>.aab"
    )
  val flavor = extractFlavor(aabName)
    ?: error("Cannot extract flavor from AAB filename '$aabName'.")
  val versionCode = readVersionCodeFromWorkspace(workspaceRoot, flavor)

  println("=== Upload Binary to Play Console ===")
  println("  AAB     : $aabName")
  println("  Version : $versionName (code: $versionCode)")
  println("  Track   : $track")
  println("  Rollout : ${rolloutFraction * 100}%")
  println()

  val accessToken = obtainAccessToken(gcpProjectId)
  val client = GooglePlayConsoleClient(accessToken)

  println("Running precondition checks...")
  PendingReleaseCheck(client).verify(PACKAGE_NAME, track)
  VersionInversionCheck(client).verify(PACKAGE_NAME, track, versionCode)
  ChangelogExistenceCheck(workspaceRoot).verify(versionName)
  println("All precondition checks passed.\n")

  println("Opening edit session...")
  val editId = client.createEdit(PACKAGE_NAME)
  println("Edit session: $editId")

  println("Uploading AAB...")
  val uploadedVersionCode = client.uploadAab(PACKAGE_NAME, editId, aabPath)
  println("Uploaded — assigned version code: $uploadedVersionCode")

  val releaseNotes = extractReleaseNotes(workspaceRoot, versionName)
  client.setTrackRelease(PACKAGE_NAME, editId, track, uploadedVersionCode, releaseNotes)
  println("Track '$track' updated.")

  client.commitEdit(PACKAGE_NAME, editId)
  println("\nDone: $versionName committed to Play Console track '$track'.")
}

/**
 * Obtains an OAuth2 access token for the Google Play Developer API via `gcloud auth print-access-token`.
 *
 * In CI this uses the Application Default Credentials set up by Workload Identity Federation.
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
 * Reads the Play Console version code for [flavor] from [VERSION_FILE] in [workspaceRoot].
 *
 * The version code is stored as a Starlark integer constant in [VERSION_FILE]:
 * `OPPIA_<FLAVOR>_VERSION_CODE = <int>`
 */
private fun readVersionCodeFromWorkspace(workspaceRoot: String, flavor: String): Long {
  val versionFile = File(workspaceRoot, VERSION_FILE)
  check(versionFile.exists()) { "version.bzl not found at '${versionFile.absolutePath}'." }

  val constantName = when (flavor.lowercase()) {
    "alpha" -> "OPPIA_ALPHA_VERSION_CODE"
    "beta" -> "OPPIA_BETA_VERSION_CODE"
    "ga" -> "OPPIA_GA_VERSION_CODE"
    else -> error("Unknown flavor '$flavor'. Expected alpha, beta, or ga.")
  }

  val pattern = Regex("""$constantName\s*=\s*(\d+)""")
  return pattern.find(versionFile.readText())?.groupValues?.get(1)?.toLongOrNull()
    ?: error("Could not find '$constantName' in ${versionFile.absolutePath}.")
}

/**
 * Extracts the version name from an AAB filename.
 *
 * Example: `oppia-android-0.18-rc00-alpha-e740815238.aab` → `0.18-rc00-alpha`
 */
private fun extractVersionName(aabName: String): String? {
  val pattern = Regex("""oppia-android-(\d+\.\d+-rc\d+-(?:alpha|beta|ga))-[0-9a-f]+\.aab""")
  return pattern.find(aabName)?.groupValues?.get(1)
}

/**
 * Extracts the build flavor from an AAB filename.
 *
 * Example: `oppia-android-0.18-rc00-alpha-e740815238.aab` → `alpha`
 */
private fun extractFlavor(aabName: String): String? {
  val pattern = Regex("""oppia-android-\d+\.\d+-rc\d+-(alpha|beta|ga)-[0-9a-f]+\.aab""")
  return pattern.find(aabName)?.groupValues?.get(1)
}

/**
 * Extracts release notes for [versionName] from [CHANGELOG_FILE] in [workspaceRoot].
 *
 * Returns a map with a single "en-US" entry containing the changelog text for the given version.
 * If no matching section is found, returns an empty map (the changelog check will have already
 * caught this case before any upload is attempted).
 */
private fun extractReleaseNotes(workspaceRoot: String, versionName: String): Map<String, String> {
  val changelogFile = File(workspaceRoot, CHANGELOG_FILE)
  if (!changelogFile.exists()) return emptyMap()

  val lines = changelogFile.readLines()
  val startIndex = lines.indexOfFirst { it.startsWith("## $versionName") }
  if (startIndex == -1) return emptyMap()

  val endIndex = lines.drop(startIndex + 1).indexOfFirst { it.startsWith("## ") }
  val sectionLines = if (endIndex == -1) {
    lines.drop(startIndex + 1)
  } else {
    lines.drop(startIndex + 1).take(endIndex)
  }

  val notes = sectionLines.joinToString("\n").trim().take(MAX_RELEASE_NOTES_LENGTH)
  return if (notes.isEmpty()) emptyMap() else mapOf("en-US" to notes)
}

private const val PACKAGE_NAME = "org.oppia.android"
private const val VERSION_FILE = "version.bzl"
private const val CHANGELOG_FILE = "CHANGELOG.md"
private const val MAX_RELEASE_NOTES_LENGTH = 500
private val VALID_TRACKS = setOf("alpha", "beta", "production")
