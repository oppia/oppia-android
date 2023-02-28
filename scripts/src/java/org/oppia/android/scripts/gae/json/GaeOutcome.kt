package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeOutcome(
  @Json(name = "dest") val dest: String?,
  @Json(name = "dest_if_really_stuck") val destIfReallyStuck: String?,
  @Json(name = "feedback") val feedback: GaeSubtitledHtml,
  @Json(name = "labelled_as_correct") val labelledAsCorrect: Boolean,
  @Json(name = "param_changes") val paramChanges: List<GaeParamChange>,
  @Json(name = "refresher_exploration_id") val refresherExplorationId: String?,
  @Json(name = "missing_prerequisite_skill_id") val missingPrerequisiteSkillId: String?
)
