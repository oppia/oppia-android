package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeWorkedExample(
  @Json(name = "question") val question: GaeSubtitledHtml,
  @Json(name = "explanation") val explanation: GaeSubtitledHtml
)
