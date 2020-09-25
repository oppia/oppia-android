package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Outcome model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/state_domain.py#L533
 */
@JsonClass(generateAdapter = true)
data class GaeOutcome(

  @Json(name = "dest") val dest: String?,
  @Json(name = "refresher_exploration_id") val refresherExplorationId: String?,
  @Json(name = "feedback") val feedback: GaeSubtitledHtml?,
  @Json(name = "param_changes") val paramChanges: List<GaeParamChange>?,
  @Json(name = "missing_prerequisite_skill_id") val missingPrerequisiteSkillId: String?,
  @Json(name = "labelled_as_correct") val isLabelledAsCorrect: Boolean?

)
