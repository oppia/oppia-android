package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Topic(

  /*
   * Ignore:
   * is_moderator, is_admin, username, user_email, iframed, is_topic_manager, is_super_admin
   */

  @Json(name = "topic_id") val topicId: String,
  @Json(name = "topic_name") val topicName: String,
  @Json(name = "canonical_story_dicts") val canonicalStoryDicts: List<CanonicalStorySummary>,
  @Json(name = "additional_story_dicts") val additionalStoryDicts: List<AdditionalStorySummary>,
  // skill_descriptions map has skill id as key and skill name as value
  @Json(name = "skill_descriptions") val skillDescriptions: Map<String, String>,
  // degrees_of_mastery map has skill id as key and a float value
  @Json(name = "degrees_of_mastery") val degreesOfMastery: Map<String, Float>,
  @Json(name = "uncategorized_skill_ids") val uncategorizedSkillIds: List<String>,
  @Json(name = "subtopics") val subtopics: List<SubtopicSummary>

)
