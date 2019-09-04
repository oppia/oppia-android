package org.oppia.app.backend.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopicSummarytDicts(

  @Json(name = "version") val version: Int?,
  @Json(name = "id") val id: String?,
  @Json(name = "name") val name: String?,
  @Json(name = "language_code") val language_code: String?,
  @Json(name = "subtopic_count") val subtopic_count: Int?,
  @Json(name = "canonical_story_count") val canonical_story_count: Int?,
  @Json(name = "uncategorized_skill_count") val uncategorized_skill_count: Int?,
  @Json(name = "additional_story_count") val additional_story_count: Int?,
  @Json(name = "total_skill_count") val total_skill_count: Int?,
  @Json(name = "topic_model_last_updated") val topic_model_last_updated: Double?,
  @Json(name = "topic_model_created_on") val topic_model_created_on: Double?

)
