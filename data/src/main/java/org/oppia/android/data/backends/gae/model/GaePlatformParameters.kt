package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for List of [GaePlatformParameter]. */
@JsonClass(generateAdapter = true)
data class GaePlatformParameters(

  @Json(name = "platform_parameters") val platformParameters: List<GaePlatformParameter>?

)
