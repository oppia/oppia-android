package org.oppia.android.scripts.release

/**
 * Version codes of OS-specific builds that must remain active on each Play Console track.
 *
 * These codes are included in every track update to prevent them from being deactivated.
 * Callers must verify that each code exists on the live track before updating it.
 *
 * Add the final version code here when freezing support for an API level or a user study.
 */
val FROZEN_VERSION_CODES_PER_TRACK: Map<String, Set<Long>> = mapOf(
  "alpha" to setOf(16L, 52377L),
  "beta" to setOf(52376L)
)

/**
 * Finds the highest non-frozen version code and the live release that contains it.
 *
 * Returns `null` when every version code in [releases] is frozen or no version codes are present.
 * Keeping the release with the code lets callers read its rollout permille from the same release.
 */
fun findHighestNonFrozenVersionCode(
  releases: List<PlayConsoleClient.TrackRelease>,
  frozenVersionCodes: Set<Long>
): Pair<PlayConsoleClient.TrackRelease, Long>? = releases.mapNotNull { release ->
  release.versionCodes.filterNot { it in frozenVersionCodes }.maxOrNull()
    ?.let { release to it }
}.maxByOrNull { it.second }
