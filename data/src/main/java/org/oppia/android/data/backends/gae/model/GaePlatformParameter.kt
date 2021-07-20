package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for a platform parameter. */
@JsonClass(generateAdapter = true)
data class GaePlatformParameter(

  /** Name of the platform parameter as stored in the backend. */
  @Json(name = "parameter_name") val parameterName: String,
  /** Value of the platform parameter as stored in the backend. */
  @Json(name = "parameter_value") val parameterValue: Any

)
