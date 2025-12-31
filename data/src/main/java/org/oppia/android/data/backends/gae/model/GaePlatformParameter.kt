package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a platform parameter configuration that can be received from Google App Engine.
 *
 * @property name the remote name for this parameter
 * @property value the server-supplied value for this parameter
 */
@JsonClass(generateAdapter = true)
data class GaePlatformParameter(
  @Json(name = "name") val name: String,
  @Json(name = "value") val value: GaePlatformParameterValue,
)
