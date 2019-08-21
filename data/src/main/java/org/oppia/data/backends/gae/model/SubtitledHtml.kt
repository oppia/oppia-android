package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubtitledHtml(

  @Json(name = "html") val html: String,
  @Json(name = "content_id") val contentId: String

)
