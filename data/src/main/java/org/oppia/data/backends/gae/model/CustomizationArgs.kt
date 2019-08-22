package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomizationArgs (

  @Json(name="parse_with_jinja") val isParseWithJinja: Boolean?,
  @Json(name="value") val value: Any?

)
