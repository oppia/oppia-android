package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SarifLocation(
  @Json(name = "physicalLocation") val physicalLocation: SarifPhysicalLocation
)
