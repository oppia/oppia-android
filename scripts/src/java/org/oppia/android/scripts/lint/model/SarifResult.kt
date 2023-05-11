package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SarifResult(
  @Json(name = "level") val level: SarifResultLevel,
  @Json(name = "locations") val locations: List<SarifLocation>,
  @Json(name = "message") val message: SarifMessage
)
