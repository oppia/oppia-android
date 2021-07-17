package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for a Platform Parameter. */
@JsonClass(generateAdapter = true)
data class GaePlatformParameter(

  @Json(name = "parameter_name") val parameterName: String,
  @Json(name = "parameter_value") val parameterValue: Any

)
