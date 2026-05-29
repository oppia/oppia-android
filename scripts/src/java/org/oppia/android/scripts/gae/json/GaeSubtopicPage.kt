package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSubtopicPage(
  @Json(name = "id") val id: String,
  @Json(name = "topic_id") val topicId: String,
  @Json(name = "sections") val sections: List<GaeStudyGuideSection>,
  @Json(name = "sections_schema_version") val sectionsSchemaVersion: Int,
  @Json(name = "next_content_id_index") val nextContentIdIndex: Int,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "version") val version: Int
)
