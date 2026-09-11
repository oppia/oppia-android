package org.oppia.android.scripts.release

/**
 * Version codes of OS-specific frozen builds that must be preserved on their respective tracks.
 *
 * The Play Developer API replaces the entire track contents on each `tracks.update` call, so any
 * release not explicitly included in the request would be silently deactivated. These are builds
 * released once to support a specific minimum API level and kept active indefinitely so devices on
 * that API level continue to receive the app.
 *
 * Frozen version codes are merged directly into the new release's `versionCodes` list on every
 * [PlayConsoleClient.setTrackRelease] call. Callers must verify that each frozen version code is
 * actually present on the live track before calling [PlayConsoleClient.setTrackRelease], and
 * hard-crash if any are missing (as that would indicate a major release state inconsistency).
 *
 * Currently frozen:
 * - alpha vc 16 : KitKat (API 16) build, frozen permanently.
 * - alpha vc 201 : Lollipop (API 21) build, frozen permanently.
 * - beta vc 196 : Lollipop (API 21) build, frozen permanently.
 *
 * When a new API level is deprecated and its final build must be frozen, add its track and version
 * code here. This single file is the source of truth consumed by [UploadBinaryToPlayConsole],
 * [UpdateRolloutFraction], and [UploadChangelogToPlayConsole].
 */
val FROZEN_VERSION_CODES_PER_TRACK: Map<String, Set<Long>> = mapOf(
  "alpha" to setOf(16L, 201L),
  "beta" to setOf(196L)
)
