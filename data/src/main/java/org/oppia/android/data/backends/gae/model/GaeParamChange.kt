package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for ParamChange model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/param_domain.py#L90
 */
@JsonClass(generateAdapter = true)
data class GaeParamChange(

  @Json(name = "generator_id") val generatorId: String?,
  @Json(name = "name") val name: String?,
  @Json(name = "customization_args") val customizationArgs: GaeCustomizationArgs?

)
