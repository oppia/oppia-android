package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for ExplorationContainer model
 * https://github.com/oppia/oppia/blob/15516a/core/controllers/reader.py#L245
 */
@JsonClass(generateAdapter = true)
data class GaeExplorationContainer(

  @Json(name = "record_playthrough_probability") val recordPlaythroughProbability: Float?,
  @Json(name = "exploration_id") val explorationId: String?,
  @Json(name = "state_classifier_mapping") val stateClassifierMapping:
  Map<String, GaeStateClassifier>?,
  @Json(name = "user_email") val userEmail: String?,
  @Json(name = "version") val version: Int?,
  @Json(name = "correctness_feedback_enabled") val isCorrectnessFeedbackEnabled: Boolean?,
  @Json(name = "username") val username: String?,
  @Json(name = "is_logged_in") val isLoggedIn: Boolean?,
  @Json(name = "exploration") val exploration: GaeExploration?,
  @Json(name = "session_id") val sessionId: String?

)
