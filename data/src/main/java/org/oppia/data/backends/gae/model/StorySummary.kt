package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Model for serialization/deserialization using Moshi with Retrofit */
@JsonClass(generateAdapter = true)
data class StorySummary(

  @Json(name = "id") val storyId: String?,
  @Json(name = "title") val title: String?,
  @Json(name = "description") val description: String?

)
