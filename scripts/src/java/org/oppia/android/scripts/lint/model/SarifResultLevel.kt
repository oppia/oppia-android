package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class SarifResultLevel(val humanName: String) {
  @Json(name = "none") NONE(humanName = "unknown"),
  @Json(name = "note") NOTE(humanName = "note"),
  @Json(name = "warning") WARNING(humanName = "warning"),
  @Json(name = "error") ERROR(humanName = "error")
}
