package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hint (

  @Json(name = "hint_content") val hintContent: SubtitledHtml?

)
