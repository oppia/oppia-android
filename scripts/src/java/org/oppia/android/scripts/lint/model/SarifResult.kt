package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * The result of a static check.
 *
 * @property level the [SarifResultLevel] of the check that was run
 * @property locations the [SarifLocation]s of files noted in the check's results
 * @property message the output [SarifMessage] representing the conclusions of the check
 */
@JsonClass(generateAdapter = true)
data class SarifResult(
  @Json(name = "level") val level: SarifResultLevel,
  @Json(name = "locations") val locations: List<SarifLocation>,
  @Json(name = "message") val message: SarifMessage
)
