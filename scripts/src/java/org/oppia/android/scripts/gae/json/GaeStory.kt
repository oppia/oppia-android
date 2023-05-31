package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeStory(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "description") val description: String,
  @Json(name = "notes") val notes: String,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "story_contents_schema_version") val storyContentsSchemaVersion: Int,
  @Json(name = "corresponding_topic_id") val correspondingTopicId: String,
  @Json(name = "version") override val version: Int,
  @Json(name = "story_contents") val storyContents: GaeStoryContents,
  @Json(name = "thumbnail_filename") val thumbnailFilename: String?,
  @Json(name = "thumbnail_bg_color") val thumbnailBgColor: String?,
  @Json(name = "thumbnail_size_in_bytes") val thumbnailSizeInBytes: Int?,
  @Json(name = "url_fragment") val urlFragment: String?,
  @Json(name = "meta_tag_content") val metaTagContent: String
) : VersionedStructure {
  fun computeReferencedExplorationIds(): Set<String> =
    storyContents.nodes.map { it.expectedExplorationId }.toSet()

  fun computeDirectlyReferencedSkillIds(): Set<String> =
    storyContents.nodes.flatMap { it.computeReferencedSkillIds() }.toSet()
}
