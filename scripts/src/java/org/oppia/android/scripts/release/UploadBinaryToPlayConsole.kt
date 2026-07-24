package org.oppia.android.scripts.release

import java.io.File

/**
 * Script that uploads a signed AAB to the Google Play Console and configures its release track.
 *
 * Before uploading, three precondition checks are run:
 * 1. No pending release exists on the target track ([PendingReleaseChecker]).
 * 2. A changelog file exists for the version being deployed ([ChangelogExistenceChecker]).
 * 3. The version code satisfies the cross-track ordering constraint (ga < beta < alpha)
 *    ([VersionInversionChecker]).
 *
 * Release notes are sourced from [CHANGELOGS_DIR]: a flavor-specific file
 * (`<major.minor>_<flavor>.md`) is used if it exists, otherwise the default (`<major.minor>.md`)
 * is used. The script fails if the changelog exceeds [MAX_RELEASE_NOTES_LENGTH] characters rather
 * than truncating silently.
 *
 * Usage (called by deploy_to_play_console.yml via Bazel):
 * ```
 * bazel run //scripts:upload_binary_to_play_console -- \
 *   <workspace_root> <aab_path> <track> <access_token> <rollout_fraction>
 * ```
 *
 * Arguments (positional):
 *   0. workspace_root   — absolute path to the repository root
 *   1. aab_path         — absolute path to the signed AAB to upload
 *   2. track            — Play Console track: "alpha", "beta", or "production"
 *   3. access_token     — OAuth2 bearer token (passed from the workflow via gcloud)
 *   4. rollout_fraction — staged rollout as an integer [0, 1000] (e.g. 250 = 25%, 1000 = 100%)
 *
 * An optional 6th argument overrides the API base URL. This is used in tests to route all
 * Play Console HTTP calls through a local [MockWebServer] instead of the real endpoint.
 */
fun main(args: Array<String>) {
  require(args.size in 5..6) {
    "Usage: upload_binary_to_play_console <workspace_root> <aab_path> <track> " +
      "<access_token> <rollout_fraction>\nGot ${args.size} argument(s): ${args.toList()}"
  }

  val workspaceRoot = args[0]
  val aabPath = args[1]
  val track = args[2]
  val accessToken = args[3]
  val rolloutFraction = requireNotNull(args[4].toIntOrNull()) {
    "rollout_fraction must be an integer in [0, 1000] (e.g. 250 for 25%), got '${args[4]}'."
  }
  val apiBaseUrl = args.getOrNull(5) ?: GooglePlayConsoleClient.PRODUCTION_API_BASE_URL

  require(track in VALID_TRACKS) {
    "track must be one of $VALID_TRACKS, got '$track'."
  }
  require(rolloutFraction in 0..1000) {
    "rollout_fraction must be between 0 and 1000, got $rolloutFraction."
  }

  val aabFile = File(aabPath)
  require(aabFile.exists()) { "AAB file not found: $aabPath" }

  val properties = parseAabFilename(aabFile.name)
    ?: error(
      "Cannot parse AAB filename '${aabFile.name}'. " +
        "Expected format: oppia-android-<major>.<minor>-rc<rc>-<flavor>-<hash>.aab"
    )

  println("=== Upload Binary to Play Console ===")
  println("  AAB     : ${aabFile.name}")
  println("  Version : ${properties.versionName} (major.minor: ${properties.majorMinorVersion})")
  println("  Flavor  : ${properties.flavor.id}")
  println("  Track   : $track")
  println("  Rollout : ${rolloutFraction / 10.0}%")
  println()

  val client = GooglePlayConsoleClient(accessToken, apiBaseUrl)
  runUpload(client, workspaceRoot, aabPath, properties, track, rolloutFraction)
}

/**
 * Executes the full upload workflow after authentication.
 *
 * @param client the [PlayConsoleClient] used for all Play Console API calls
 * @param workspaceRoot absolute path to the repository root (for changelog lookups)
 * @param aabPath absolute path to the signed AAB to upload
 * @param properties parsed properties from the AAB filename
 * @param track the Play Console track ("alpha", "beta", or "production")
 * @param rolloutFraction the rollout fraction as an integer in [0, 1000]
 */
private fun runUpload(
  client: PlayConsoleClient,
  workspaceRoot: String,
  aabPath: String,
  properties: AabProperties,
  track: String,
  rolloutFraction: Int
) {
  println("Running pre-upload precondition checks...")
  PendingReleaseChecker(client).verify(PACKAGE_NAME, track)
  println("Pending release check passed: no in-flight releases on track '$track'.")
  ChangelogExistenceChecker(workspaceRoot).verify(
    properties.majorVersion, properties.minorVersion, properties.flavor
  )
  println(
    "Changelog existence check passed for ${properties.majorMinorVersion} " +
      "(${properties.flavor.id})."
  )
  println("Pre-upload checks passed.")
  println()

  println("Opening edit session...")
  val editId = client.createEdit(PACKAGE_NAME)
  println("Edit session: $editId")

  println("Uploading AAB...")
  val uploadedVersionCode = client.uploadAab(PACKAGE_NAME, editId, aabPath)
  println("Uploaded — assigned version code: $uploadedVersionCode")

  println("Running version inversion check...")
  VersionInversionChecker(client).verify(PACKAGE_NAME, track, uploadedVersionCode, editId)
  println("Version inversion check passed.")
  println()

  val releaseNotes = extractReleaseNotes(
    workspaceRoot,
    properties.majorMinorVersion,
    properties.flavor.id
  )
  client.setTrackRelease(
    PACKAGE_NAME,
    editId,
    track,
    uploadedVersionCode,
    rolloutFraction,
    releaseNotes
  )
  println("Track '$track' updated.")

  client.commitEdit(PACKAGE_NAME, editId)
  println()
  println(
    "Done: ${properties.versionName} (vc=$uploadedVersionCode) " +
      "committed to Play Console track '$track'."
  )
}

/**
 * Represents properties parsed from a signed AAB filename.
 *
 * @property majorVersion the major version number
 * @property minorVersion the minor version number
 * @property rcNumber the release candidate number string (e.g. "01")
 * @property flavor the build flavor
 * @property versionName the full version string (e.g. "0.18-rc01-alpha")
 * @property majorMinorVersion the major.minor string (e.g. "0.18")
 */
data class AabProperties(
  val majorVersion: Int,
  val minorVersion: Int,
  val rcNumber: String,
  val flavor: AppFlavor,
  val versionName: String,
  val majorMinorVersion: String
)

private val AAB_FILENAME_REGEX =
  Regex("""oppia-android-(\d+)\.(\d+)-rc(\d+)-(alpha|beta|ga)-[0-9a-f]+\.aab""")

/**
 * Parses all relevant properties from a signed AAB filename in a single regex pass.
 *
 * Example: `oppia-android-0.18-rc01-alpha-e740815230.aab`
 * → [AabProperties] with majorVersion=0, minorVersion=18, rcNumber="01", flavor=ALPHA
 *
 * @return parsed [AabProperties], or `null` if the filename does not match the expected format
 */
private fun parseAabFilename(aabName: String): AabProperties? {
  val match = AAB_FILENAME_REGEX.find(aabName) ?: return null
  val (major, minor, rc, flavorId) = match.destructured
  val flavor = AppFlavor.fromId(flavorId) ?: return null
  val majorMinorVersion = "$major.$minor"
  val versionName = "$majorMinorVersion-rc$rc-$flavorId"
  return AabProperties(
    majorVersion = major.toInt(),
    minorVersion = minor.toInt(),
    rcNumber = rc,
    flavor = flavor,
    versionName = versionName,
    majorMinorVersion = majorMinorVersion
  )
}

/**
 * Reads the release notes for [majorMinorVersion] and [flavorId] from [CHANGELOGS_DIR].
 *
 * Lookup order:
 * 1. `config/changelogs/<majorMinorVersion>_<flavorId>.md` (flavor-specific override)
 * 2. `config/changelogs/<majorMinorVersion>.md` (default)
 *
 * Fails if the changelog text exceeds [MAX_RELEASE_NOTES_LENGTH] characters.
 * Returns an empty map if no file is found.
 */
private fun extractReleaseNotes(
  workspaceRoot: String,
  majorMinorVersion: String,
  flavorId: String
): Map<String, String> {
  val changelogsDir = File(workspaceRoot, CHANGELOGS_DIR)
  val flavorSpecificFile = File(changelogsDir, "${majorMinorVersion}_$flavorId.md")
  val defaultFile = File(changelogsDir, "$majorMinorVersion.md")

  val changelogFile = when {
    flavorSpecificFile.exists() -> flavorSpecificFile
    defaultFile.exists() -> defaultFile
    else -> return emptyMap()
  }

  val notes = changelogFile.readText().trim()
  check(notes.length <= MAX_RELEASE_NOTES_LENGTH) {
    "Changelog '${changelogFile.name}' exceeds the $MAX_RELEASE_NOTES_LENGTH character " +
      "limit (${notes.length} chars). Trim it before deploying."
  }
  return if (notes.isEmpty()) emptyMap() else mapOf("en-US" to notes)
}

private const val PACKAGE_NAME = "org.oppia.android"
private const val CHANGELOGS_DIR = "config/changelogs"
private const val MAX_RELEASE_NOTES_LENGTH = 500
private val VALID_TRACKS = setOf("alpha", "beta", "production")
