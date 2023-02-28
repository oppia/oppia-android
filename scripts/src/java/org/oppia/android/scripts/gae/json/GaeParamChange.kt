package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// TODO: implement customization_args adapter.
@JsonClass(generateAdapter = true)
data class GaeParamChange(
  @Json(name = "name") val name: String,
  @Json(name = "generator_id") val generatorId: String,
  @Json(name = "customization_args") val customizationArgs: GaeParamCustomizationArgs
)
