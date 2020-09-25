package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for ParamSpec model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/param_domain.py#L47
 */
@JsonClass(generateAdapter = true)
data class GaeParamSpec(

  @Json(name = "obj_type") val objType: String?

)
