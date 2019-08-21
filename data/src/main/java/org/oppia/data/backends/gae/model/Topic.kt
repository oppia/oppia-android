package org.oppia.data.backends.gae.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Topic(

  /*
   * Ignore:
   * is_moderator, is_admin, username, user_email, iframed, is_topic_manager, is_super_admin
   */

  val topic_id: String,
  val topic_name: String,
  val canonical_story_dicts: List<CanonicalStorySummary>,
  val additional_story_dicts: List<AdditionalStorySummary>,
  // skill_descriptions map has skill id as key and skill name as value
  val skill_descriptions: Map<String, String>,
  // degrees_of_mastery map has skill id as key and a float value
  val degrees_of_mastery: Map<String, Float>,
  val uncategorized_skill_ids: List<String>,
  val subtopics: List<SubtopicSummary>

)