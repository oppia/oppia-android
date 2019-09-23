package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for CustomizationArgs model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/param_domain.py#L99
 */
@JsonClass(generateAdapter = true)
data class GaeCustomizationArgs(

  @Json(name = "parse_with_jinja") val isParseWithJinja: Boolean?,
  @Json(name = "value") val value: Any?

)
