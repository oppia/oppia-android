package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for TopicSummaryDict model containing full information
 * https://github.com/oppia/oppia/blob/77d6fd/core/domain/topic_domain.py#L1178
 */
@JsonClass(generateAdapter = true)
data class GaeTopicSummary(

  @Json(name = "version") val version: Int?,
  @Json(name = "id") val id: String?,
  @Json(name = "name") val name: String?,
  @Json(name = "subtopic_count") val subtopic_count: Int?,
  @Json(name = "canonical_story_count") val canonicalStoryCount: Int?,
  @Json(name = "uncategorized_skill_count") val uncategorizedSkillCount: Int?,
  @Json(name = "additional_story_count") val additionalStoryCount: Int?,
  @Json(name = "total_skill_count") val totalSkillCount: Int?,
  @Json(name = "topic_model_last_updated") val topicModelLastUpdated: Double?,
  @Json(name = "topic_model_created_on") val topicModelCreatedOn: Double?

)
