package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Exploration (

  /*
    * Ignore below params
    * language_code
    */

  @Json(name = "states") val states: Map<String, State>?,
  @Json(name = "param_changes") val paramChanges: List<ParamChange>?,
  @Json(name = "param_specs") val paramSpecs: Map<String, ParamSpec>?,
  @Json(name = "init_state_name") val initStateName: String?,
  @Json(name = "objective") val objective: String?,
  @Json(name = "correctness_feedback_enabled") val isCorrectnessFeedbackEnabled: Boolean?,
  @Json(name = "title") val title: String?

)
