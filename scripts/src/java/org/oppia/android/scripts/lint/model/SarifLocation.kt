package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the location of a file included in the results of a static check.
 *
 * @property physicalLocation the [SarifPhysicalLocation] of the affected file
 */
@JsonClass(generateAdapter = true)
data class SarifLocation(
  @Json(name = "physicalLocation") val physicalLocation: SarifPhysicalLocation
)
