package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a human-readable message representing the results of a static check.
 *
 * @property text the specific human-readable text that describes the check results
 */
@JsonClass(generateAdapter = true)
data class SarifMessage(@Json(name = "text") val text: String)
