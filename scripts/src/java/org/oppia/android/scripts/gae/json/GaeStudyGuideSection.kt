package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeStudyGuideSection(
  @Json(name = "heading") val heading: GaeSubtitledUnicode,
  @Json(name = "content") val content: GaeSubtitledHtml
)
