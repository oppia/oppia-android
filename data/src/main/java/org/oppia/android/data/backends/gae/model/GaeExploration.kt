package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for Exploration model
 * https://github.com/oppia/oppia/blob/15516a/core/domain/exp_domain.py#L3272
 */
@JsonClass(generateAdapter = true)
data class GaeExploration(

  @Json(name = "states") val states: Map<String, GaeState>?,
  @Json(name = "param_changes") val paramChanges: List<GaeParamChange>?,
  @Json(name = "param_specs") val paramSpecs: Map<String, GaeParamSpec>?,
  @Json(name = "init_state_name") val initStateName: String?,
  @Json(name = "objective") val objective: String?,
  @Json(name = "correctness_feedback_enabled") val isCorrectnessFeedbackEnabled: Boolean?,
  @Json(name = "title") val title: String?

)
