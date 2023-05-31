package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeTopic(
  @Json(name = "id") val id: String,
  @Json(name = "name") val name: String,
  @Json(name = "abbreviated_name") val abbreviatedName: String?,
  @Json(name = "url_fragment") val urlFragment: String?,
  @Json(name = "thumbnail_filename") val thumbnailFilename: String?,
  @Json(name = "thumbnail_bg_color") val thumbnailBgColor: String?,
  @Json(name = "thumbnail_size_in_bytes") val thumbnailSizeInBytes: Int?,
  @Json(name = "description") val description: String,
  @Json(name = "canonical_story_references") val canonicalStoryRefs: List<GaeStoryReference>,
  @Json(name = "additional_story_references") val additionalStoryRefs: List<GaeStoryReference>,
  @Json(name = "uncategorized_skill_ids") val uncategorizedSkillIds: List<String>,
  @Json(name = "subtopics") val subtopics: List<GaeSubtopic>,
  @Json(name = "subtopic_schema_version") val subtopicSchemaVersion: Int,
  @Json(name = "next_subtopic_id") val nextSubtopicId: Int,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "version") override val version: Int,
  @Json(name = "story_reference_schema_version") val storyReferenceSchemaVersion: Int,
  @Json(name = "meta_tag_content") val metaTagContent: String,
  @Json(name = "practice_tab_is_displayed") val practiceTabIsDisplayed: Boolean,
  @Json(name = "page_title_fragment_for_web") val pageTitleFragmentForWeb: String?,
  @Json(name = "skill_ids_for_diagnostic_test") val skillIdsForDiagnosticTest: List<String>
) : VersionedStructure {
  fun computeContainedSubtopicMap(): Map<Int, GaeSubtopic> = subtopics.associateBy { it.id }

  fun computeReferencedStoryIds(): Set<String> = canonicalStoryRefs.map { it.storyId }.toSet()

  fun computeDirectlyReferencedSkillIds(): Set<String> =
    (subtopics.flatMap { it.skillIds } + uncategorizedSkillIds).toSet()
}
