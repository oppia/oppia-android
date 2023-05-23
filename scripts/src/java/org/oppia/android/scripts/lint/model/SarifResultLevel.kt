package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents different levels of a static check.
 *
 * @property humanName the human-readable representation of the check
 */
@JsonClass(generateAdapter = false)
enum class SarifResultLevel(val humanName: String) {
  /** Indicates that the level was unknown. */
  @Json(name = "none") NONE(humanName = "unknown"),

  /** Indicates that the check has notes, but not failures. */
  @Json(name = "note") NOTE(humanName = "note"),

  /** Indicates that the check has found warnings. */
  @Json(name = "warning") WARNING(humanName = "warning"),

  /** Indicates that the check has found errors. */
  @Json(name = "error") ERROR(humanName = "error")
}
