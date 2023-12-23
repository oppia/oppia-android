package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeExploration(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "category") val category: String,
  @Json(name = "author_notes") val author_notes: String,
  @Json(name = "blurb") val blurb: String,
  @Json(name = "states_schema_version") val statesSchemaVersion: Int,
  @Json(name = "init_state_name") val initStateName: String,
  @Json(name = "language_code") val languageCode: String,
  @Json(name = "objective") val objective: String,
  @Json(name = "param_changes") val paramChanges: List<GaeParamChange>,
  @Json(name = "param_specs") val paramSpecs: Map<String, GaeParamSpec>,
  @Json(name = "tags") val tags: List<String>,
  @Json(name = "auto_tts_enabled") val autoTtsEnabled: Boolean,
  @Json(name = "correctness_feedback_enabled") val correctnessFeedbackEnabled: Boolean,
  @Json(name = "next_content_id_index") val nextContentIdIndex: Int,
  @Json(name = "edits_allowed") val editsAllowed: Boolean,
  @Json(name = "states") val states: Map<String, GaeState>,
  @Json(name = "version") val version: Int
) {
  fun computeDirectlyReferencedSkillIds(): Set<String> =
    states.values.flatMap { it.computeReferencedSkillIds() }.toSet()
}
