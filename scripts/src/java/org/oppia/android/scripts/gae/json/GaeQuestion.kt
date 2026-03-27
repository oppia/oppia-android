package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeQuestion(
  @Json(name = "id") val id: String,
  @Json(name = "question_state_data") val questionStateData: GaeStateWithoutRecordedVoiceovers,
  @Json(name = "question_state_data_schema_version") val questionStateDataSchemaVersion: Int,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "linked_skill_ids") val linkedSkillIds: List<String>,
  @Json(name = "inapplicable_skill_misconception_ids") val inapplicableSkillMisconceptionIds: List<String>,
  @Json(name = "next_content_id_index") val nextContentIdIndex: Int,
  @Json(name = "version") val version: Int
)
