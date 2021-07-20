package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for a list of [GaePlatformParameter]s evaluated by Oppia's backend. */
@JsonClass(generateAdapter = true)
data class GaePlatformParameterList(

  /** List of all the platform parameters received from the backend. */
  @Json(name = "platform_parameters") val platformParameters: List<GaePlatformParameter>?

)
