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
