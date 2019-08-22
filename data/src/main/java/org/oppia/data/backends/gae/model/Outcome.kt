package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Outcome(

  @Json(name = "dest") val dest: String?,
  @Json(name = "refresher_exploration_id") val refresherExplorationId: String?,
  @Json(name = "feedback") val feedback: SubtitledHtml?,
  @Json(name = "param_changes") val paramChanges: List<ParamChange>?,
  @Json(name = "missing_prerequisite_skill_id") val missingPrerequisiteSkillId: String?,
  @Json(name = "labelled_as_correct") val isLabelledAsCorrect: Boolean?

)
