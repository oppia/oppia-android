package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSubtopicPage(
  @Json(name = "id") val id: String,
  @Json(name = "topic_id") val topicId: String,
  @Json(name = "page_contents") val pageContents: GaeSubtopicPageContents,
  @Json(name = "page_contents_schema_version") val pageContentsSchemaVersion: Int,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "version") val version: Int
)
