package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSubtitledHtml(
  @Json(name = "content_id") override val contentId: String,
  @Json(name = "html") override val text: String
) : SubtitledText
