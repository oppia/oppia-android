package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for StoryNode model
 * https://github.com/oppia/oppia/blob/b56a20/core/domain/story_domain.py#L226
 */
@JsonClass(generateAdapter = true)
data class GaeStoryNode(

  @Json(name = "id") val id: String?,
  @Json(name = "title") val title: String?,
  @Json(name = "destination_node_ids") val destinationNodeIds: List<String>?,
  @Json(name = "acquired_skill_ids") val acquiredSkillIds: List<String>?,
  @Json(name = "prerequisite_skill_ids") val prerequisiteSkillIds: List<String>?,
  @Json(name = "outline") val outline: String?,
  @Json(name = "outline_is_finalized") val isOutlineFinalized: Boolean?,
  @Json(name = "exploration_id") val explorationId: String?,
  @Json(name = "exp_summary_dict") val explorationSummaryDict: GaeExpSummary?,
  @Json(name = "completed") val isCompleted: Boolean?

)
