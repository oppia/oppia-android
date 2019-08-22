package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParamChange(

  @Json(name = "generator_id") val generatorId: String?,
  @Json(name = "name") val name: String?,
  @Json(name = "customization_args") val customizationArgs: CustomizationArgs?

)
