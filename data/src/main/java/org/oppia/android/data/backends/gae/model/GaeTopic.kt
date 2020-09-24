package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Topic model containing full information
 * https://github.com/oppia/oppia/blob/b33aa9/core/controllers/topic_viewer.py#L45
 */
@JsonClass(generateAdapter = true)
data class GaeTopic(

  @Json(name = "topic_id") val topicId: String?,
  @Json(name = "topic_name") val topicName: String?,
  @Json(name = "canonical_story_dicts") val canonicalStoryDicts: List<GaeStorySummary?>?,
  @Json(name = "additional_story_dicts") val additionalStoryDicts: List<GaeStorySummary?>?,
  /** A map of skill descriptions keyed by skill ID. */
  @Json(name = "skill_descriptions") val skillDescriptions: Map<String, String?>?,
  /** degrees_of_mastery map has skill id as key and a float value */
  /** A map of degree masteries keyed by skill ID. */
  @Json(name = "degrees_of_mastery") val degreesOfMastery: Map<String, Float?>?,
  @Json(name = "uncategorized_skill_ids") val uncategorizedSkillIds: List<String?>?,
  @Json(name = "subtopics") val subtopics: List<GaeSubtopicSummary?>?

)
