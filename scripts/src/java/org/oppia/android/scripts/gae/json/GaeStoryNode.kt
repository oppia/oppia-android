package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeStoryNode(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "description") val description: String,
  @Json(name = "thumbnail_filename") val thumbnailFilename: String?,
  @Json(name = "thumbnail_bg_color") val thumbnailBgColor: String?,
  @Json(name = "thumbnail_size_in_bytes") val thumbnailSizeInBytes: Int?,
  @Json(name = "destination_node_ids") val destinationNodeIds: List<String>,
  @Json(name = "acquired_skill_ids") val acquiredSkillIds: List<String>,
  @Json(name = "prerequisite_skill_ids") val prerequisiteSkillIds: List<String>,
  @Json(name = "outline") val outline: String,
  @Json(name = "outline_is_finalized") val outlineIsFinalized: Boolean,
  @Json(name = "exploration_id") val explorationId: String?
) {
  val expectedExplorationId: String by lazy {
    checkNotNull(explorationId) { "Expected node to have exploration ID: $this." }
  }

  fun computeReferencedSkillIds(): List<String> = acquiredSkillIds + prerequisiteSkillIds
}
