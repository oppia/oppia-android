package org.oppia.android.scripts.release

/**
 * Version codes of OS-specific frozen builds that must be preserved on their respective tracks.
 *
 * The Play Developer API replaces the entire track contents on each `tracks.update` call, so any
 * release not explicitly included in the request would be silently deactivated. These are builds
 * released once to support a specific minimum API level and kept active indefinitely so devices on
 * that API level continue to receive the app.
 *
 * Releases are fetched live from the Play Console and filtered against these version codes. Matching
 * releases are then passed through completely unmodified (preserving versionCodes, status,
 * userFraction, and releaseNotes) into every [PlayConsoleClient.setTrackRelease] call.
 *
 * Currently frozen:
 * - alpha vc 16: KitKat (API 16) build, frozen permanently.
 *
 * When a new API level is deprecated and its final build must be frozen, add its track and version
 * code here. This single file is the source of truth consumed by [UploadBinaryToPlayConsole],
 * [UpdateRolloutFraction], [UploadChangelogToPlayConsole], and [GooglePlayConsoleClient].
 */
val FROZEN_VERSION_CODES_PER_TRACK: Map<String, Set<Long>> = mapOf(
  "alpha" to setOf(16L)
)
