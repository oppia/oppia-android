package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSkill(
  @Json(name = "id") val id: String,
  @Json(name = "description") val description: String,
  @Json(name = "misconceptions") val misconceptions: List<GaeMisconception>,
  @Json(name = "rubrics") val rubrics: List<GaeRubric>,
  @Json(name = "skill_contents") val skillContents: GaeSkillContents,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "misconceptions_schema_version") val misconceptionsSchemaVersion: Int,
  @Json(name = "rubric_schema_version") val rubricSchemaVersion: Int,
  @Json(name = "skill_contents_schema_version") val skillContentsSchemaVersion: Int,
  @Json(name = "version") val version: Int,
  @Json(name = "next_misconception_id") val nextMisconceptionId: Int,
  @Json(name = "superseding_skill_id") val supersedingSkillId: String?,
  @Json(name = "all_questions_merged") val allQuestionsMerged: Boolean,
  @Json(name = "prerequisite_skill_ids") val prerequisiteSkillIds: List<String>
) {
  fun computeDirectlyReferencedSkillIds(): Set<String> =
    (listOfNotNull(supersedingSkillId) + prerequisiteSkillIds).toSet()
}
