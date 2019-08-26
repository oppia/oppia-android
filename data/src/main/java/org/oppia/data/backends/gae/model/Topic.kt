package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Model for serialization/deserialization using Moshi with Retrofit *
 * @see <a href="https://github.com/oppia/oppia/blob/b33aa9cf1aa6372e12d0b35f95cceb44efe5320f/core/controllers/topic_viewer.py#L45">Oppia-Web Topic structure</a>
 */
@JsonClass(generateAdapter = true)
data class Topic(

  @Json(name = "topic_id") val topicId: String?,
  @Json(name = "topic_name") val topicName: String?,
  @Json(name = "canonical_story_dicts") val canonicalStoryDicts: List<StorySummary?>?,
  @Json(name = "additional_story_dicts") val additionalStoryDicts: List<StorySummary?>?,
  /** skill_descriptions map has skill id as key and skill name as value */
  @Json(name = "skill_descriptions") val skillDescriptions: Map<String, String?>?,
  /** degrees_of_mastery map has skill id as key and a float value */
  @Json(name = "degrees_of_mastery") val degreesOfMastery: Map<String, Float?>?,
  @Json(name = "uncategorized_skill_ids") val uncategorizedSkillIds: List<String?>?,
  @Json(name = "subtopics") val subtopics: List<SubtopicSummary?>?

)
