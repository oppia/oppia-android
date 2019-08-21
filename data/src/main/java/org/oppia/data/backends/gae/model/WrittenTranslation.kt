package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WrittenTranslation(

  @Json(name = "html") val html: String,
  @Json(name = "needs_update") val isUpdateNeeded: Boolean

)
